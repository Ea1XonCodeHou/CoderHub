package com.eaxon.coderhubserver.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubcommon.constant.RedisConstant;
import com.eaxon.coderhubpojo.VO.ChatConversationVO;
import com.eaxon.coderhubpojo.VO.ChatMsgVO;
import com.eaxon.coderhubpojo.entity.ChatConversation;
import com.eaxon.coderhubpojo.entity.ChatMsg;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ChatMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.ChatService;
import com.eaxon.coderhubserver.service.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * 私信聊天 ServiceImpl
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    /** 系统助手虚拟用户ID（不存在于 user 表，前端特殊渲染） */
    public static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000001";
    public static final String SYSTEM_USER_NAME = "CoderHub 助手";
    public static final String SYSTEM_AVATAR = null; // 前端用图标渲染，无需URL

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    // ==================== 会话 ====================

    @Override
    public List<ChatConversationVO> getConversations(String currentUserId) {
        List<ChatConversation> rawConvs = chatMapper.findUserConversations(currentUserId);

        List<ChatConversationVO> result = new ArrayList<>();

        // 检查是否已有系统会话
        boolean hasSystemConv = rawConvs.stream().anyMatch(c ->
                SYSTEM_USER_ID.equals(c.getUser1Id()) || SYSTEM_USER_ID.equals(c.getUser2Id()));

        if (!hasSystemConv) {
            // 首次使用：创建系统欢迎会话
            createSystemWelcome(currentUserId);
            rawConvs = chatMapper.findUserConversations(currentUserId);
        }

        for (ChatConversation conv : rawConvs) {
            String peerId = conv.getUser1Id().equals(currentUserId) ? conv.getUser2Id() : conv.getUser1Id();

            ChatConversationVO vo = ChatConversationVO.builder()
                    .id(conv.getId())
                    .peerId(peerId)
                    .lastMessage(conv.getLastMessage())
                    .lastMsgAt(conv.getLastMsgAt())
                    .unreadCount(chatMapper.countUnreadInConversation(conv.getId(), currentUserId))
                    .build();

            // 填充对方信息
            if (SYSTEM_USER_ID.equals(peerId)) {
                vo.setPeerName(SYSTEM_USER_NAME);
                vo.setPeerAvatar(SYSTEM_AVATAR);
                vo.setPeerLevel(0);
            } else {
                User peer = userMapper.getUserById(peerId);
                if (peer != null) {
                    vo.setPeerName(peer.getUsername());
                    vo.setPeerAvatar(peer.getAvatar());
                    vo.setPeerLevel(peer.getUserLevel());
                }
            }

            result.add(vo);
        }

        // 系统会话置顶
        result.sort((a, b) -> {
            if (SYSTEM_USER_ID.equals(a.getPeerId())) return -1;
            if (SYSTEM_USER_ID.equals(b.getPeerId())) return 1;
            if (b.getLastMsgAt() == null) return -1;
            if (a.getLastMsgAt() == null) return 1;
            return b.getLastMsgAt().compareTo(a.getLastMsgAt());
        });

        return result;
    }

    @Override
    public String getOrCreateConversationId(String currentUserId, String targetUserId) {
        // 规范化：user1Id < user2Id（字典序），保证唯一
        String u1 = currentUserId.compareTo(targetUserId) < 0 ? currentUserId : targetUserId;
        String u2 = currentUserId.compareTo(targetUserId) < 0 ? targetUserId : currentUserId;

        ChatConversation existing = chatMapper.findConversationByUsers(u1, u2);
        if (existing != null) {
            return existing.getId();
        }

        // 新建会话
        ChatConversation newConv = ChatConversation.builder()
                .id(UUID.randomUUID().toString())
                .user1Id(u1)
                .user2Id(u2)
                .createdAt(LocalDateTime.now())
                .build();
        chatMapper.insertConversation(newConv);
        log.info("创建新私信会话: {} <-> {}, convId={}", u1, u2, newConv.getId());
        return newConv.getId();
    }

    // ==================== 消息 ====================

    @Override
    public List<ChatMsgVO> getMessages(String conversationId, String currentUserId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ChatMsg> msgs = chatMapper.findMessages(conversationId, offset, pageSize);

        List<ChatMsgVO> result = new ArrayList<>();
        for (ChatMsg msg : msgs) {
            ChatMsgVO vo = buildMsgVO(msg, currentUserId);
            result.add(vo);
        }
        return result;
    }

    @Override
    public ChatMsgVO saveAndPushMessage(String senderId, String receiverId, String content) {
        String convId = getOrCreateConversationId(senderId, receiverId);

        ChatMsg msg = ChatMsg.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(convId)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .isRead(0)
                .createdAt(LocalDateTime.now())
                .build();
        chatMapper.insertMessage(msg);

        // 更新会话最新消息预览
        String preview = content.length() > 50 ? content.substring(0, 50) + "…" : content;
        chatMapper.updateLastMessage(convId, preview, msg.getCreatedAt());

        // 更新接收方 Redis 未读计数
        String redisKey = RedisConstant.CHAT_UNREAD + receiverId;
        redisService.increment(redisKey, 1L);

        log.info("私信已保存: {} -> {}, convId={}", senderId, receiverId, convId);
        return buildMsgVO(msg, senderId);
    }

    @Override
    public void markAsRead(String conversationId, String currentUserId) {
        int unread = chatMapper.countUnreadInConversation(conversationId, currentUserId);
        if (unread > 0) {
            chatMapper.markConversationAsRead(conversationId, currentUserId);
            // 扣减 Redis 未读计数（不低于0）
            String redisKey = RedisConstant.CHAT_UNREAD + currentUserId;
            Object val = redisService.get(redisKey);
            if (val != null) {
                long current = Long.parseLong(val.toString());
                long newVal = Math.max(0, current - unread);
                redisService.set(redisKey, newVal);
            }
        }
    }

    @Override
    public int getUnreadCount(String userId) {
        String redisKey = RedisConstant.CHAT_UNREAD + userId;
        Object cached = redisService.get(redisKey);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }
        // Redis miss：从数据库查询并回填
        int count = chatMapper.countUnread(userId);
        redisService.set(redisKey, count);
        return count;
    }

    // ==================== 私有工具 ====================

    /**
     * 构建消息 VO，填充发送方信息
     */
    private ChatMsgVO buildMsgVO(ChatMsg msg, String currentUserId) {
        String senderName;
        String senderAvatar;

        if (SYSTEM_USER_ID.equals(msg.getSenderId())) {
            senderName = SYSTEM_USER_NAME;
            senderAvatar = SYSTEM_AVATAR;
        } else {
            User sender = userMapper.getUserById(msg.getSenderId());
            senderName = sender != null ? sender.getUsername() : "未知用户";
            senderAvatar = sender != null ? sender.getAvatar() : null;
        }

        return ChatMsgVO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .createdAt(msg.getCreatedAt())
                .isMine(currentUserId.equals(msg.getSenderId()))
                .build();
    }

    /**
     * 为新用户创建系统欢迎会话及欢迎消息
     */
    private void createSystemWelcome(String userId) {
        String convId = getOrCreateConversationId(SYSTEM_USER_ID, userId);

        String welcomeText = "👋 欢迎来到 CoderHub！这里是你的信息中心，你可以在这里接收系统通知，也可以与其他创作者私信交流。祝你在这里记录成长、结交朋友！";

        ChatMsg welcome = ChatMsg.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(convId)
                .senderId(SYSTEM_USER_ID)
                .receiverId(userId)
                .content(welcomeText)
                .isRead(0)
                .createdAt(LocalDateTime.now())
                .build();
        chatMapper.insertMessage(welcome);
        chatMapper.updateLastMessage(convId, welcomeText.substring(0, 50) + "…", welcome.getCreatedAt());

        log.info("系统欢迎消息已发送给用户: {}", userId);
    }
}
