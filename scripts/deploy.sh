#!/bin/bash

set -e

echo "새 컨테이너 배포 중..."

# 서비스 이름 확인
if [ -z "$SERVICE_NAME" ]; then
    echo "오류: SERVICE_NAME 환경변수가 설정되지 않았습니다"
    exit 1
fi

TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"
STATE_FILE="${TARGET_DIR}/blue-green-state"

# 상태 정보 로드
if [ ! -f "$STATE_FILE" ]; then
    echo "오류: 배포 상태 파일을 찾을 수 없습니다"
    exit 1
fi

source "$STATE_FILE"

cd "$TARGET_DIR"

# 새 컨테이너 배포
echo "$STANDBY_ENV 환경 배포 중 (포트: $STANDBY_PORT)..."
STANDBY_PORT=$STANDBY_PORT docker-compose -p ${SERVICE_NAME}-${STANDBY_ENV} up -d --pull always

echo "$STANDBY_ENV 환경 배포 완료"