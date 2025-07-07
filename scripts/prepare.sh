#!/bin/bash

set -e

SERVICE_NAME=${SERVICE_NAME:-spring-prod}
TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"

PORT_BLUE=8080
PORT_GREEN=8081

echo "Blue-Green 배포 준비 중..."

if [ ! -d "$TARGET_DIR" ]; then
    echo "오류: 대상 디렉토리가 존재하지 않습니다: $TARGET_DIR"
    exit 1
fi

cd "$TARGET_DIR"

echo "GHCR 로그인 중..."
echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_ACTOR" --password-stdin

get_active_environment() {
    if docker ps --format "table {{.Names}}\t{{.Ports}}" | grep -E "(^|-)${SERVICE_NAME}(-|$)" | grep -q ":${PORT_BLUE}->"; then
        echo "blue"
    elif docker ps --format "table {{.Names}}\t{{.Ports}}" | grep -E "(^|-)${SERVICE_NAME}(-|$)" | grep -q ":${PORT_GREEN}->"; then
        echo "green"
    else
        echo "blue"
    fi
}

get_standby_environment() {
    local active_env=$1
    if [ "$active_env" = "blue" ]; then
        echo "green"
    else
        echo "blue"
    fi
}

get_port_by_environment() {
    local env=$1
    if [ "$env" = "blue" ]; then
        echo "$PORT_BLUE"
    else
        echo "$PORT_GREEN"
    fi
}

ACTIVE_ENV=$(get_active_environment)
STANDBY_ENV=$(get_standby_environment $ACTIVE_ENV)
ACTIVE_PORT=$(get_port_by_environment $ACTIVE_ENV)
STANDBY_PORT=$(get_port_by_environment $STANDBY_ENV)

echo "ACTIVE_ENV=$ACTIVE_ENV" >> ${TARGET_DIR}/blue-green-state
echo "STANDBY_ENV=$STANDBY_ENV" >> ${TARGET_DIR}/blue-green-state
echo "ACTIVE_PORT=$ACTIVE_PORT" >> ${TARGET_DIR}/blue-green-state
echo "STANDBY_PORT=$STANDBY_PORT" >> ${TARGET_DIR}/blue-green-state
echo "SERVICE_NAME=$SERVICE_NAME" >> ${TARGET_DIR}/blue-green-state

echo "현재 활성: $ACTIVE_ENV (포트: $ACTIVE_PORT)"
echo "배포 대상: $STANDBY_ENV (포트: $STANDBY_PORT)"

echo "기존 $STANDBY_ENV 환경 정리 중..."
docker-compose -p ${SERVICE_NAME}-${STANDBY_ENV} down || true

echo "배포 준비 완료"