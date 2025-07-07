#!/bin/bash

set -e

echo "새 환경 헬스체크 중..."

if [ -z "$SERVICE_NAME" ]; then
    echo "오류: SERVICE_NAME 환경변수가 설정되지 않았습니다"
    exit 1
fi

TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"
STATE_FILE="${TARGET_DIR}/blue-green-state"

if [ ! -f "$STATE_FILE" ]; then
    echo "오류: 배포 상태 파일을 찾을 수 없습니다"
    exit 1
fi

source "$STATE_FILE"

health_check() {
    local port=$1
    local max_attempts=30
    local attempt=1
    
    echo "$STANDBY_ENV 환경 (포트: $port) 헬스체크 중..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "$STANDBY_ENV 환경 헬스체크 성공 (포트: $port)"
            return 0
        fi
        echo "헬스체크 시도 $attempt/$max_attempts 실패, 10초 대기..."
        sleep 10
        ((attempt++))
    done
    
    echo "$STANDBY_ENV 환경 헬스체크 실패 (포트: $port)"
    return 1
}

if ! health_check $STANDBY_PORT; then
    echo "$STANDBY_ENV 환경 헬스체크 실패"
    exit 1
fi

echo "$STANDBY_ENV 환경이 포트 $STANDBY_PORT에서 정상 동작 중입니다"