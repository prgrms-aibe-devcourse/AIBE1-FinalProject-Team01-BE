#!/bin/bash
set -e

APP_PORT=${APP_PORT:-8080}

if [ -z "$APP_PORT" ]; then
  echo "오류: APP_PORT 환경변수가 설정되지 않았습니다"
  exit 1
fi

echo "헬스체크 수행 중 (포트: $APP_PORT)..."
sleep 5

for i in {1..5}; do
  if curl -f -s "http://localhost:$APP_PORT/actuator/health" > /dev/null 2>&1; then
    echo "헬스체크 성공 - 애플리케이션이 정상 작동 중입니다"
    exit 0
  fi
  echo "헬스체크 실패, $i번째 시도..."
  sleep 10
done

echo "헬스체크 실패 - 애플리케이션이 정상 작동하지 않습니다"
exit 1