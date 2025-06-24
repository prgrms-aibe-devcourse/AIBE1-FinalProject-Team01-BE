#!/usr/bin/env node

const https = require('https');
const url = require('url');

// 환경변수에서 GitHub 컨텍스트 정보 가져오기
const context = {
  repository: process.env.GITHUB_REPOSITORY,
  branch: process.env.GITHUB_REF_NAME,
  actor: process.env.GITHUB_ACTOR,
  trigger: process.env.GITHUB_EVENT_NAME,
  workflowUrl: `${process.env.GITHUB_SERVER_URL}/${process.env.GITHUB_REPOSITORY}/actions/runs/${process.env.GITHUB_RUN_ID}`
};

// 명령행 인수 파싱
const [,, workflowType, status, webhookUrl, detailsJson = '{}'] = process.argv;

if (!workflowType || !status || !webhookUrl) {
  console.error('❌ 필수 인수가 누락되었습니다: workflowType, status, webhookUrl');
  process.exit(1);
}

// 제목 생성 함수
function generateTitle(type, status) {
  const titles = {
    ci: status === 'success' ? '✅ CI Pipeline 성공' : '❌ CI Pipeline 실패',
    cd: status === 'success' ? '🚀 배포 성공' : '💥 배포 실패',
    sync: status === 'success' ? '🔄 설정 동기화 완료' : '❌ 설정 동기화 실패',
    build: status === 'success' ? '🔨 빌드 성공' : '🔨 빌드 실패',
    test: status === 'success' ? '🧪 테스트 통과' : '🧪 테스트 실패'
  };
  
  return titles[type] || (status === 'success' ? `✅ ${type} 성공` : `❌ ${type} 실패`);
}

// 메시지 생성 함수
function generateMessage(context, status, details) {
  let message = [
    `* **Repository:** ${context.repository}`,
    `* **Branch:** ${context.branch}`,
    `* **Actor:** ${context.actor}`,
    `* **Trigger:** ${context.trigger}`,
    `* **Status:** ${status}`
  ];

  // 상세 정보 추가
  if (Object.keys(details).length > 0) {
    message.push('');
    message.push('✅ **Details:**');
    Object.entries(details).forEach(([key, value]) => {
      const formattedKey = key.replace(/_/g, ' ').replace(/^./, c => c.toUpperCase());
      message.push(`* **${formattedKey}:** ${value}`);
    });
  }

  message.push('');
  message.push(`🔗 **Workflow:** ${context.workflowUrl}`);

  return message.join('\n');
}

// 웹훅 전송 함수
function sendWebhook(url, payload) {
  return new Promise((resolve, reject) => {
    const urlObj = new URL(url);
    const data = JSON.stringify(payload);
    
    const options = {
      hostname: urlObj.hostname,
      port: urlObj.port || (urlObj.protocol === 'https:' ? 443 : 80),
      path: urlObj.pathname + urlObj.search,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data)
      }
    };
    
    const req = (urlObj.protocol === 'https:' ? https : require('http')).request(options, (res) => {
      let responseData = '';
      
      res.on('data', (chunk) => {
        responseData += chunk;
      });
      
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve({ statusCode: res.statusCode, data: responseData });
        } else {
          reject(new Error(`HTTP ${res.statusCode}: ${res.statusMessage}`));
        }
      });
    });
    
    req.on('error', (error) => {
      reject(error);
    });
    
    req.write(data);
    req.end();
  });
}

// 메인 실행 함수
async function main() {
  try {
    // 상세 정보 파싱
    let details = {};
    try {
      if (detailsJson && detailsJson !== '{}') {
        details = JSON.parse(detailsJson);
      }
    } catch (error) {
      console.warn('⚠️ Details JSON 파싱 실패:', error.message);
      details = {};
    }
    
    // 페이로드 생성
    const payload = {
      title: generateTitle(workflowType, status),
      message: generateMessage(context, status, details),
      status: status
    };
    
    console.log('📤 웹훅 전송 중...');
    console.log('Payload:', JSON.stringify(payload, null, 2));
    
    // 웹훅 전송
    const response = await sendWebhook(webhookUrl, payload);
    
    console.log('✅ 웹훅 전송 성공');
    console.log(`Response: ${response.statusCode}`);
    
  } catch (error) {
    console.error('❌ 웹훅 전송 실패:', error.message);
    process.exit(1);
  }
}

// 스크립트 실행
main();
