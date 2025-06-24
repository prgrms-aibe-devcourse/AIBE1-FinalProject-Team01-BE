#!/bin/bash
set -e

echo "헬스체크 수행 중..."
sleep 30

for i in {1..5}; do
  if curl -f -s "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
    echo "헬스체크 성공 - 애플리케이션이 정상 작동 중입니다"
    exit 0
  fi
  echo "헬스체크 실패, $i번째 시도..."
  sleep 10
done

echo "헬스체크 실패 - 애플리케이션이 정상 작동하지 않습니다"
exit 1