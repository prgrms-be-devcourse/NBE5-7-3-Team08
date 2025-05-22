import React from 'react';

const MessageItem = ({ msg, currentUser, contextMenuId, setContextMenuId, setEditMessageId, setEditContent, handleEditMessage, handleDeleteMessage }) => {
    const formatDate = (dateString) => {
        const date = new Date(dateString);

        if (isNaN(date.getTime())) {
            return new Date().toLocaleDateString('ko-KR', {
                year: 'numeric', month: '2-digit', day: '2-digit'
            });
        }

        return date.toLocaleDateString('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit'
        });
    };

    
  const renderWithLink = (text) => {
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    return text.split(urlRegex).map((part, i) =>
      urlRegex.test(part) ? (
        <a
          key={i}
          href={part}
          target="_blank"
          rel="noopener noreferrer"
          style={{ color: '#0366d6', textDecoration: 'underline' }}
        >
          {part}
        </a>
      ) : (
        part
      )
    );
  };

  return (
       <div key={`msg-${index}`} style={{
          marginBottom: '18px',
          display: 'flex',
          alignItems: 'flex-start',
        }}>
          {/* 프로필 이미지 */}
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            marginRight: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontWeight: '600',
            fontSize: '16px',
            flexShrink: 0,
            backgroundImage: `url("http://localhost:8080/images/profile/${msg.profileImageUrl}")`,
            backgroundSize: 'cover'
          }}>
          </div>
          <div style={{ flex: 1, maxWidth: 'calc(100% - 50px)' }}>
            <div style={{
              display: 'flex',
              marginBottom: '6px',
              justifyContent: 'space-between',
            }}>
              <div style={{ display: 'flex', alignItems: 'baseline' }}>
                <span style={{
                  fontWeight: '600',
                  fontSize: '15px',
                  color: '#2d3748'
                }}>
                  {msg.senderName}
                </span>
                <span style={{
                  fontWeight: 'normal',
                  fontSize: '12px',
                  color: '#718096',
                  marginLeft: '8px'
                }}>
                  {new Date(msg.sendAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>

              {/* 점 세개 메뉴는 조건부 렌더링 */}
              {currentUser?.id === msg.senderId && !msg.deleted && msg.type !== 'GIT' && (
                <div style={{ position: 'relative' }}>
                  <button
                    onClick={() =>
                      setContextMenuId(contextMenuId === msg.messageId ? null : msg.messageId)
                    }
                    style={{
                      position: 'absolute',
                      right: '0px',
                      border: 'none',
                      background: 'transparent',
                      cursor: 'pointer',
                      fontSize: '18px',
                      color: '#94a3b8'
                    }}
                  >
                    ⋯
                  </button>

                  {contextMenuId === msg.messageId && (
                    <div style={{
                      position: 'absolute',
                      top: '24px',
                      right: '0',
                      backgroundColor: '#fff',
                      border: '1px solid #e2e8f0',
                      borderRadius: '6px',
                      boxShadow: '0 2px 6px rgba(0,0,0,0.15)',
                      zIndex: 1000,
                      padding: '6px 0',
                      minWidth: '140px'
                    }}>
                      {/* 수정 버튼은 이미지 메시지가 아닌 경우에만 표시 */}
                      {msg.type !== 'IMAGE' && (
                        <>
                          <button
                            onClick={() => {
                              setEditMessageId(msg.messageId);
                              setEditContent(msg.content);
                              setContextMenuId(null);
                            }}
                            style={{
                              display: 'block',
                              width: '100%',
                              padding: '10px 16px',
                              textAlign: 'left',
                              background: 'none',
                              border: 'none',
                              fontSize: '14px',
                              cursor: 'pointer'
                            }}
                          >
                            메세지 수정하기
                          </button>

                          {/* 구분선 추가 */}
                          <div style={{
                            height: '1px',
                            backgroundColor: '#e2e8f0',
                            margin: '0 8px'
                          }} />
                        </>
                      )}

                      <button
                        onClick={() => {
                          const confirmed = window.confirm("정말 삭제하시겠습니까?");
                          if (confirmed) {
                            handleDeleteMessage(msg.messageId);
                          }
                          setContextMenuId(null);
                        }}
                        style={{
                          display: 'block',
                          width: '100%',
                          padding: '10px 16px',
                          textAlign: 'left',
                          background: 'none',
                          border: 'none',
                          fontSize: '14px',
                          color: '#e53e3e',
                          cursor: 'pointer'
                        }}
                      >
                        삭제
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 본문 영역 - 수정 중인 메시지는 textarea, 나머지는 content 렌더 */}
            {editMessageId === msg.messageId && msg.type !== 'GIT' ? (
              <div style={{ display: 'flex', gap: '8px', flexDirection: 'column' }}>
                <textarea
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  style={{
                    width: '100%',
                    minHeight: '80px',
                    border: '1px solid #e2e8f0',
                    borderRadius: '6px',
                    padding: '10px',
                    fontSize: '14px',
                    resize: 'vertical'
                  }}
                />
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                  <button
                    onClick={() => handleEditMessage(msg.messageId)}
                    style={{
                      backgroundColor: '#4a6cf7',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      padding: '6px 12px',
                      fontSize: '14px',
                      cursor: 'pointer'
                    }}
                  >
                    저장
                  </button>
                  <button
                    onClick={() => {
                      setEditMessageId(null);
                      setEditContent('');
                    }}
                    style={{
                      backgroundColor: '#e2e8f0',
                      color: '#1a202c',
                      border: 'none',
                      borderRadius: '4px',
                      padding: '6px 12px',
                      fontSize: '14px',
                      cursor: 'pointer'
                    }}
                  >
                    취소
                  </button>
                </div>
              </div>
            )
              :(msg.deleted || msg.isDeleted) ? (
                <div style={{
                  fontSize: '14px',
                  lineHeight: '1.5',
                  color: '#a0aec0',
                  fontStyle: 'italic'
                }}>
                  삭제된 메시지입니다.
                </div>
              )
                : msg.type === 'GIT' ? (
                  <div style={{
                    backgroundColor: '#f6f8fa',
                    borderRadius: '6px',
                    color: '#24292e',
                    display: 'flex'
                  }}>
                    {/* 왼쪽 검정색 선 */}
                    <div style={{
                      width: '6px',
                      backgroundColor: '#000',
                      marginRight: '10px',
                      borderRadius: '2px'
                    }} />
                    {msg.content && (
                      <div style={{ whiteSpace: 'pre-wrap', lineHeight: '1.5', padding: '10px' }}>
                        {msg.content.split('\n').map((line, i) => (
                          <div key={i}>
                            {i === 0 ? (
                              <strong>{renderWithLink(line)}</strong>
                            ) : (
                              <>{renderWithLink(line)}</>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ) : msg.type === 'CODE' || (msg.content && msg.content.startsWith('```')) ? (
                  <div style={{
                    borderRadius: '6px',
                    overflow: 'hidden',
                    border: '1px solid #e2e8f0'
                  }}>
                    <HighlightedCode
                      content={msg.content.replace(/```/g, '')}
                      language={msg.language || 'java'}
                    />
                    {msg.edited && (
                      <span style={{
                        marginLeft: '6px',
                        fontSize: '11px',
                        color: '#a0aec0',
                        fontStyle: 'italic'
                      }}>
                        (수정됨)
                      </span>
                    )}
                  </div>
                ) : msg.type === 'IMAGE' ? (
                  <div style={{
                    maxWidth: '30%',
                    backgroundColor: '#f8fafc',
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    padding: '8px',
                    boxShadow: '0 1px 4px rgba(0, 0, 0, 0.05)'
                  }}>
                    <img
                      src={`http://localhost:8080/images/chat/${msg.chatImageUrl}`}
                      alt="업로드된 이미지"
                      style={{
                        width: '100%',
                        maxHeight: '400px',
                        objectFit: 'contain',
                        borderRadius: '6px'
                      }}
                    />
                  </div>
                )
                  : (
                    <div style={{
                      fontSize: '14px',
                      lineHeight: '1.5',
                      color: '#4a5568',
                      wordBreak: 'break-word',
                      whiteSpace: 'pre-wrap'
                    }}>
                      {msg.content}
                      {(msg.edited || msg.isEdited) && (
                        <span style={{
                          marginLeft: '6px',
                          fontSize: '11px',
                          color: '#a0aec0',
                          fontStyle: 'italic'
                        }}>
                          (수정됨)
                        </span>
                      )}
                    </div>
                  )}
          </div>
        </div>
  )

}


const MessageList = ({ messages, currentUser, contextMenuId, setContextMenuId, setEditMessageId, setEditContent, handleEditMessage, handleDeleteMessage }) => {
      if (!messages.length) return null;

    const result = [];
    let currentDate = null;

    // 메시지를 순회하며 날짜별로 구분
    messages.forEach((msg, index) => {
      const messageDate = formatDate(msg.sendAt);

      // 날짜가 바뀌었다면 구분선 추가
      if (messageDate !== currentDate) {
        currentDate = messageDate;
        result.push(
          <div key={`date-${index}`}
            style={{ display: 'flex',
                alignItems: 'center',
                margin: '24px 0',
                color: '#64748b',
                fontSize: '14px',
                fontWeight: '500'
          }}>
            <div style={{
              flex: '1',
              height: '1px',
              backgroundColor: '#e2e8f0'
            }} />
            <div style={{
              margin: '0 16px',
              padding: '4px 12px',
              backgroundColor: '#f8fafc',
              borderRadius: '12px',
              border: '1px solid #e2e8f0'
            }}>
              {messageDate}
            </div>
            <div style={{
              flex: '1',
              height: '1px',
              backgroundColor: '#e2e8f0'
            }}/>
          </div>
        );
      }

      // 메시지 추가
      result.push(
        <div key={`msg-${index}`} style={{
          marginBottom: '18px',
          display: 'flex',
          alignItems: 'flex-start',
        }}>
          {/* 프로필 이미지 */}
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            marginRight: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontWeight: '600',
            fontSize: '16px',
            flexShrink: 0,
            backgroundImage: `url("http://localhost:8080/images/profile/${msg.profileImageUrl}")`,
            backgroundSize: 'cover'
          }}>
          </div>
          <div style={{ flex: 1, maxWidth: 'calc(100% - 50px)' }}>
            <div style={{
              display: 'flex',
              marginBottom: '6px',
              justifyContent: 'space-between',
            }}>
              <div style={{ display: 'flex', alignItems: 'baseline' }}>
                <span style={{
                  fontWeight: '600',
                  fontSize: '15px',
                  color: '#2d3748'
                }}>
                  {msg.senderName}
                </span>
                <span style={{
                  fontWeight: 'normal',
                  fontSize: '12px',
                  color: '#718096',
                  marginLeft: '8px'
                }}>
                  {new Date(msg.sendAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>

}