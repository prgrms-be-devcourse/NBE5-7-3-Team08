import { useEffect, useRef } from "react";
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useNavigate } from 'react-router-dom';

const useWebSocket = ({
    roomId,
    onMessageReceived,
    chatRooms = [],
    currentRoomId,
    onSidebarMessage,
    onProfileUpdate,
}) => {
    const stompClientRef = useRef(null);
    const subscriptionRef = useRef(null);
    const profileSubscriptionRef = useRef(null);
    const hasConnectedRef = useRef(false);
    const sidebarSubscriptionsRef = useRef(new Map());
    const keepAliveIntervalRef = useRef(null);

    const navigate = useNavigate();

    // WebSocket 연결 초기화 (한 번만 실행)
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            reconnectDelay: 1000,
            heartbeatIncoming: 15000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log(`[STOMP] ${str}`),

            onConnect: () => {
                console.log('✅ Connected to WebSocket');
                hasConnectedRef.current = true;

                // 프로필 업데이트 구독 (한 번만 설정)
                if (onProfileUpdate && !profileSubscriptionRef.current) {
                    profileSubscriptionRef.current = client.subscribe('/topic/profile-update', (message) => {
                        try {
                            const profileUpdate = JSON.parse(message.body);
                            console.log('🔥 프로필 업데이트 수신:', profileUpdate);
                            onProfileUpdate(profileUpdate);
                        } catch (e) {
                            console.error("📛 Failed to parse profile update message", e);
                        }
                    });
                    console.log('👤 프로필 업데이트 구독 완료');
                }

                // Keep-alive 설정
                if (keepAliveIntervalRef.current) clearInterval(keepAliveIntervalRef.current);
                keepAliveIntervalRef.current = setInterval(() => {
                    if (client && client.connected) {
                        client.publish({
                            destination: '/app/ping',
                            body: 'ping'
                        });
                        console.log("📡 Sent keep-alive ping");
                    }
                }, 20000);
            },
            
            onStompError: (frame) => {
                console.error("💥 STOMP error:", frame.headers['message']);
                if (frame.headers['message']?.includes('Unauthorized') || frame.body?.includes('expired')) {
                    navigate("/login");
                }
            }
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            console.log("🧹 Cleaning up WebSocket...");

            if (keepAliveIntervalRef.current) {
                clearInterval(keepAliveIntervalRef.current);
                keepAliveIntervalRef.current = null;
                console.log("🔕 Stopped keep-alive ping");
            }

            // 모든 구독 해제
            if (subscriptionRef.current) {
                subscriptionRef.current.unsubscribe();
                subscriptionRef.current = null;
            }

            if (profileSubscriptionRef.current) {
                profileSubscriptionRef.current.unsubscribe();
                profileSubscriptionRef.current = null;
            }

            sidebarSubscriptionsRef.current.forEach((subscription) => {
                subscription.unsubscribe();
            });
            sidebarSubscriptionsRef.current.clear();

            if (client && client.active) {
                client.deactivate().then(() => {
                    console.log("🛑 Disconnected from WebSocket");
                });
            }
        };
    }, []); // 의존성 배열을 비워서 한 번만 실행

    // 메인 채팅방 구독 관리 (roomId 변경 시에만)
    useEffect(() => {
        const client = stompClientRef.current;
        if (!client || !client.connected || !roomId || !onMessageReceived) {
            console.log('⏳ 메인 채팅방 구독 대기 중...');
            return;
        }

        console.log(`🔄 메인 채팅방 구독 변경: ${roomId}`);

        // 기존 메인 채팅방 구독 해제
        if (subscriptionRef.current) {
            subscriptionRef.current.unsubscribe();
            console.log("🔁 Previous main subscription cleared.");
        }

        // 새로운 메인 채팅방 구독
        subscriptionRef.current = client.subscribe(`/topic/chat/${roomId}`, (message) => {
            try {
                const received = JSON.parse(message.body);
                received.sendAt = received.sendAt || new Date().toISOString();
                onMessageReceived(received);
            } catch (e) {
                console.error("📛 Failed to parse incoming message", e);
            }
        });

        console.log(`📡 메인 채팅방 구독 완료: ${roomId}`);
    }, [roomId, onMessageReceived]);

    // 사이드바 채팅방들 구독 관리 (chatRooms 변경 시에만)
    useEffect(() => {
        const client = stompClientRef.current;
        if (!client || !client.connected || chatRooms.length === 0 || !onSidebarMessage) {
            console.log('⏳ 사이드바 구독 대기 중...');
            return;
        }

        console.log('🔄 사이드바 구독 목록 업데이트');

        // 기존 사이드바 구독들 정리
        sidebarSubscriptionsRef.current.forEach((subscription, roomId) => {
            subscription.unsubscribe();
            console.log(`🔁 Previous sidebar subscription for room ${roomId} cleared.`);
        });
        sidebarSubscriptionsRef.current.clear();

        // 모든 채팅방에 대해 구독 설정
        chatRooms.forEach(room => {
            const roomUniqueId = room.uniqueId;
            if (roomUniqueId) {
                const subscription = client.subscribe(`/topic/chat/${roomUniqueId}`, (message) => {
                    try {
                        const received = JSON.parse(message.body);
                        
                        // 현재 있는 채팅방이 아닌 경우에만 사이드바 알림 처리
                        if (Number(currentRoomId) !== Number(roomUniqueId)) {
                            onSidebarMessage(roomUniqueId, received);
                            console.log(`📨 New message in room ${roomUniqueId}`);
                        }
                    } catch (e) {
                        console.error("📛 Failed to parse sidebar message", e);
                    }
                });
                
                sidebarSubscriptionsRef.current.set(roomUniqueId, subscription);
                console.log(`📡 Subscribed to sidebar room: ${roomUniqueId}`);
            }
        });
    }, [chatRooms, currentRoomId, onSidebarMessage]);

    return stompClientRef;
};

export default useWebSocket;