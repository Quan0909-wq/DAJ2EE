package com.example.demoj2ee.service;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DatingService {

    @Autowired private DatingProfileRepository datingProfileRepository;
    @Autowired private DatingRequestRepository datingRequestRepository;
    @Autowired private DatingMatchRepository datingMatchRepository;
    @Autowired private DatingMessageRepository datingMessageRepository;
    @Autowired private DatingNotificationRepository datingNotificationRepository;
    @Autowired private UserRepository userRepository;

    // ============ PROFILE ============

    public DatingProfile getOrCreateProfile(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must be logged in with a persisted id");
        }
        Long uid = user.getId();
        return datingProfileRepository.findByUserId(uid)
            .orElseGet(() -> {
                User managed = userRepository.findById(uid)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + uid));
                DatingProfile p = new DatingProfile(managed);
                return datingProfileRepository.save(p);
            });
    }

    public Optional<DatingProfile> getProfileByUserId(Long userId) {
        return datingProfileRepository.findByUserId(userId);
    }

    public DatingProfile saveProfile(DatingProfile profile) {
        profile.setUpdatedAt(LocalDateTime.now());
        return datingProfileRepository.save(profile);
    }

    public List<DatingProfile> getActiveProfiles(Long currentUserId) {
        return datingProfileRepository.findByIsActiveTrue().stream()
            .filter(p -> !p.getUser().getId().equals(currentUserId))
            .toList();
    }

    // ============ REQUESTS ============

    @Transactional
    public Map<String, Object> sendRequest(User fromUser, Long toUserId) {
        Map<String, Object> result = new HashMap<>();

        DatingProfile fromProfile = datingProfileRepository.findByUser(fromUser).orElse(null);
        if (fromProfile == null || !fromProfile.isProfileComplete()) {
            result.put("error", "Bạn cần hoàn thiện hồ sơ dating trước!");
            return result;
        }

        Optional<User> toUserOpt = userRepository.findById(toUserId);
        if (toUserOpt.isEmpty() || toUserOpt.get().getId().equals(fromUser.getId())) {
            result.put("error", "Không thể gửi lời mời cho chính mình.");
            return result;
        }

        boolean alreadyMatch = datingMatchRepository.existsByUser1IdAndUser2Id(fromUser.getId(), toUserId)
            || datingMatchRepository.existsByUser1IdAndUser2Id(toUserId, fromUser.getId());
        if (alreadyMatch) {
            result.put("error", "Hai bạn đã match rồi!");
            return result;
        }

        DatingProfile toProfile = datingProfileRepository.findByUserId(toUserId).orElse(null);
        if (toProfile == null) {
            result.put("error", "Người này chưa có hồ sơ dating.");
            return result;
        }

        DatingRequest request = new DatingRequest(fromUser, toUserOpt.get(), fromProfile, toProfile);
        datingRequestRepository.save(request);

        // Notify receiver
        DatingNotification notif = new DatingNotification(toProfile.getUser(), fromUser, "REQUEST_RECEIVED");
        notif.setDatingRequest(request);
        datingNotificationRepository.save(notif);

        result.put("success", true);
        result.put("message", "Đã gửi lời mời kết bạn!");
        return result;
    }

    @Transactional
    public Map<String, Object> respondToRequest(User user, Long requestId, boolean accept) {
        Map<String, Object> result = new HashMap<>();

        DatingRequest request = datingRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            result.put("error", "Lời mời không tồn tại.");
            return result;
        }

        if (!request.getToUser().getId().equals(user.getId())) {
            result.put("error", "Bạn không có quyền phản hồi lời mời này.");
            return result;
        }

        if (!"PENDING".equals(request.getStatus())) {
            result.put("error", "Lời mời đã được phản hồi.");
            return result;
        }

        request.setStatus(accept ? "ACCEPTED" : "REJECTED");
        request.setUpdatedAt(LocalDateTime.now());
        datingRequestRepository.save(request);

        if (accept) {
            // Create match
            DatingMatch match = new DatingMatch(
                request.getFromUser(),
                request.getToUser(),
                request.getFromProfile(),
                request.getToProfile(),
                request
            );
            datingMatchRepository.save(match);

            // Notify sender
            DatingNotification notif1 = new DatingNotification(request.getFromUser(), user, "MATCHED");
            notif1.setDatingMatch(match);
            datingNotificationRepository.save(notif1);

            DatingNotification notif2 = new DatingNotification(user, request.getFromUser(), "MATCHED");
            notif2.setDatingMatch(match);
            datingNotificationRepository.save(notif2);

            result.put("success", true);
            result.put("matched", true);
            result.put("message", "Match thành công! Hãy bắt đầu trò chuyện.");
        } else {
            // Notify sender of rejection
            DatingNotification notif = new DatingNotification(request.getFromUser(), user, "REQUEST_REJECTED");
            notif.setDatingRequest(request);
            datingNotificationRepository.save(notif);

            result.put("success", true);
            result.put("matched", false);
            result.put("message", "Đã từ chối lời mời.");
        }

        return result;
    }

    // ============ MATCHES ============

    public List<DatingMatch> getUserMatches(Long userId) {
        return datingMatchRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    public Optional<DatingMatch> getMatchById(Long matchId, Long userId) {
        return datingMatchRepository.findById(matchId)
            .filter(m -> m.getUser1().getId().equals(userId) || m.getUser2().getId().equals(userId));
    }

    // ============ MESSAGES ============

    public List<DatingMessage> getMessages(Long matchId, Long userId) {
        Optional<DatingMatch> matchOpt = datingMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) return Collections.emptyList();
        DatingMatch match = matchOpt.get();
        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            return Collections.emptyList();
        }
        return datingMessageRepository.findByMatchIdOrderByCreatedAtAsc(matchId);
    }

    @Transactional
    public DatingMessage sendMessage(Long matchId, Long senderId, String content) {
        Optional<DatingMatch> matchOpt = datingMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) return null;
        DatingMatch match = matchOpt.get();

        User sender = match.getUser1().getId().equals(senderId) ? match.getUser1() : match.getUser2();
        User receiver = match.getUser1().getId().equals(senderId) ? match.getUser2() : match.getUser1();

        DatingMessage message = new DatingMessage(match, sender, content);
        datingMessageRepository.save(message);

        // Notify receiver
        DatingNotification notif = new DatingNotification(receiver, sender, "NEW_MESSAGE");
        notif.setDatingMatch(match);
        datingNotificationRepository.save(notif);

        return message;
    }

    // ============ NOTIFICATIONS ============

    public List<DatingNotification> getUnshownNotifications(Long userId) {
        return datingNotificationRepository.findByToUserIdAndIsShownFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnshownNotifications(Long userId) {
        return datingNotificationRepository.countByToUserIdAndIsShownFalse(userId);
    }

    @Transactional
    public void dismissNotification(Long notificationId, Long userId) {
        datingNotificationRepository.findById(notificationId)
            .filter(n -> n.getToUser().getId().equals(userId))
            .ifPresent(n -> {
                n.setShown(true);
                n.setRead(true);
                datingNotificationRepository.save(n);
            });
    }

    @Transactional
    public void dismissAllNotifications(Long userId) {
        datingNotificationRepository.findByToUserIdAndIsShownFalseOrderByCreatedAtDesc(userId)
            .forEach(n -> {
                n.setShown(true);
                n.setRead(true);
                datingNotificationRepository.save(n);
            });
    }

    // ============ PENDING REQUESTS ============

    public List<DatingRequest> getPendingRequests(Long userId) {
        return datingRequestRepository.findByToUserIdAndStatus(userId, "PENDING");
    }

    public List<DatingRequest> getSentRequests(Long userId) {
        return datingRequestRepository.findByFromUserIdAndToUserId(userId, userId)
            .stream().filter(r -> "PENDING".equals(r.getStatus())).toList();
    }

    public boolean hasPendingRequest(Long fromUserId, Long toUserId) {
        return datingRequestRepository.existsByFromUserIdAndToUserIdAndStatus(fromUserId, toUserId, "PENDING");
    }

    public boolean hasMatched(Long user1Id, Long user2Id) {
        return datingMatchRepository.existsByUser1IdAndUser2Id(user1Id, user2Id)
            || datingMatchRepository.existsByUser1IdAndUser2Id(user2Id, user1Id);
    }

    public Optional<Long> getMatchIdBetweenUsers(Long userId1, Long userId2) {
        return datingMatchRepository.findByUser1IdAndUser2Id(userId1, userId2)
            .or(() -> datingMatchRepository.findByUser1IdAndUser2Id(userId2, userId1))
            .map(DatingMatch::getId);
    }
}
