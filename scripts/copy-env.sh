#!/bin/bash

set -e

SERVICE_NAME=${SERVICE_NAME:-spring-prod}
TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"

echo "=== 환경변수 파일 생성 시작 ==="

if [[ -z "$SECRETS_JSON" ]]; then
    echo "SECRETS_JSON 환경변수가 설정되지 않았습니다."
    exit 1
fi

# 서비스 디렉토리 생성 (없으면)
mkdir -p "$TARGET_DIR"

# 타겟 디렉토리로 이동
cd "$TARGET_DIR"

echo "$SECRETS_JSON" | jq -r '
    to_entries[] |
    select(.key | test("^(ACTIONS_|GITHUB_|github_)") | not) |
    "\(.key)=\(.value)"
' > .env

echo "환경변수 파일 생성 완료"
ls -la .env
