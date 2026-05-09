# Created: 2026-05-09 22:27:52
# 배포 시행착오 기록

배포 파이프라인 구축 및 운영 과정에서 겪은 문제와 해결 방법을 시간순으로 정리합니다.

---

## 1단계 — CI/CD 파이프라인 초기 구축 (Apr 27~28)

### Docker 관련 파일 없음

**문제**: Dockerfile, docker-compose 파일이 프로젝트에 없었음  
**해결**: Spring Boot 앱용 Dockerfile 신규 작성, Jenkinsfile에 ECR push 단계 구성

---

### Jenkins 빌드 서버에 AWS CLI 미설치

**문제**: Docker Build & ECR Push 단계에서 `aws: not found` 오류  
**원인**: Jenkins 빌드 서버 자체에 AWS CLI가 설치되어 있지 않았음  
**해결**: Jenkins 서버에 AWS CLI v2 설치

---

### 배포 대상 EC2에도 AWS CLI 미설치

**문제**: Deploy 단계에서 SSH로 EC2에 접속 후 `aws ecr get-login-password` 실행 시 `aws: not found`  
**원인**: 빌드 서버와 배포 서버는 별개 — 배포 대상 EC2에도 별도로 AWS CLI 설치 필요  
**해결**: 백엔드 EC2에 AWS CLI v2 설치

---

### ECR 레포지토리 이름 불일치

**문제**: `The repository 'nemonemo/nemonemo-backend' does not exist`  
**원인**: `ECR_REGISTRY` credential 값에 경로가 포함되어 있는데, `ECR_REPO`에 `nemonemo-backend`를 넣어 `nemonemo/nemonemo-backend`로 조합됨. 실제 레포 이름은 `nemonemo`  
**해결**: `ECR_REPO` 값을 `nemonemo`로 수정

---

### EC2 EBS 볼륨 확장 후 용량 그대로

**문제**: AWS 콘솔에서 EBS 볼륨 크기를 늘렸는데 `df -h`에서 용량이 그대로  
**원인**: AWS는 볼륨 크기만 확장할 뿐, OS 내부 파티션과 파일시스템은 자동으로 확장하지 않음  
**해결**:
```bash
sudo growpart /dev/xvda 1       # 파티션 확장
sudo resize2fs /dev/xvda1       # 파일시스템 확장 (ext4)
# xfs의 경우: sudo xfs_growfs /
```

---

## 2단계 — Jenkins Credentials & 첫 배포 (Apr 29)

### Jenkins Credentials 미등록

**문제**: `Could not find credentials entry with ID 'PROD_DB_USERNAME'`  
**원인**: Jenkinsfile에서 참조하는 시크릿들을 Jenkins Credentials Store에 등록하지 않았음  
**해결**: Jenkins > Manage Jenkins > Credentials에 아래 항목들을 Secret text로 등록

| Credential ID | 타입 | 내용 |
|---|---|---|
| `PROD_DB_URL` | Secret text | Supabase JDBC URL |
| `PROD_DB_USERNAME` | Secret text | DB 사용자명 |
| `PROD_DB_PASSWORD` | Secret text | DB 비밀번호 |
| `PROD_JWT_SECRET` | Secret text | JWT 서명 키 |
| `BACKEND_HOST` | Secret text | EC2 퍼블릭 IP |
| `ECR_REGISTRY` | Secret text | ECR 레지스트리 주소 |
| `AWS_CREDENTIALS` | AWS Credentials | AWS Access Key/Secret |
| `SSH_KEY` | SSH Username with private key | EC2 접속용 키페어 |

---

### 배포 성공 후 API ECONNREFUSED

**문제**: Jenkins 배포 성공 후 Postman으로 API 호출 시 `ECONNREFUSED`  
**원인**: EC2 보안그룹에 8080 포트 인바운드 규칙이 없었음 (SSH 22만 열려 있었음)  
**해결**: AWS 콘솔 > EC2 > 보안그룹 > 인바운드 규칙에 TCP 8080 추가

---

## 3단계 — Supabase 연결 삽질 (Apr 29 ~ May 7)

가장 많은 시간이 걸린 구간. 여러 원인이 겹쳐 있었음.

---

### Supabase DNS가 IPv6 주소 반환 → EC2 연결 불가

**문제**: Docker 컨테이너에서 Supabase 직접 연결 DNS가 IPv6 주소만 반환, EC2가 IPv6 미지원으로 연결 실패  
**해결**: Jenkinsfile `docker run` 명령에 JVM 옵션 추가

```
-e JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"
```

---

### IPv4 강제 후에도 연결 실패 지속

**문제**: `-Djava.net.preferIPv4Stack=true` 적용 후 재배포했으나 Supabase 연결 계속 실패  
**원인**: 진짜 원인은 Jenkinsfile의 `docker run` 명령에 DB 환경변수 자체가 누락되어 있었음 (`-e DB_URL`, `-e DB_USERNAME` 등이 없었음)  
**해결**: Jenkinsfile Deploy 단계에 환경변수 주입 옵션 추가

---

### Pooler 연결 시 `ENOIDENTIFIER`

**문제**: `PSQLException FATAL (ENOIDENTIFIER): no tenant identifier provided`  
**원인**: Supabase Connection Pooler는 멀티테넌트 구조라 username에 프로젝트 식별자 포함 필요  
**해결**: `PROD_DB_USERNAME` 형식 변경

```
변경 전: postgres
변경 후: postgres.{project-ref}
```

---

### `ENOTFOUND: tenant/user not found`

**문제**: username 형식을 맞췄는데도 `ENOTFOUND`  
**원인**: project-ref가 틀렸거나 Supabase 프로젝트가 Paused 상태여서 Pooler에 미등록  
**해결**: Supabase 대시보드 > Settings > Database > Connection pooling 섹션에서 JDBC URL을 직접 복사해 정확한 값 확인

---

### Pooler 리전 오류

**문제**: `ap-northeast-2` 리전 Pooler URL을 사용했으나 연결 안 됨  
**원인**: 실제 Supabase 프로젝트의 Pooler 리전은 `aws-1-ap-south-1`이었음  
**최종 연결 성공 설정**:

| 항목 | 값 |
|---|---|
| `PROD_DB_URL` | `jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?prepareThreshold=0` |
| `PROD_DB_USERNAME` | `postgres.{project-ref}` |
| 포트 | 6543 (Pooler), 5432 아님 |

---

## 4단계 — 서비스 품질 개선 (May 7~9)

### Vercel 프론트엔드에서 API 호출 실패 (CORS)

**문제**: 프론트엔드를 Vercel에 배포했더니 모든 API 호출이 CORS 오류로 실패  
**원인**: `SecurityConfig.java`에 CORS 설정이 전혀 없었음  
**해결**: `CorsConfigurationSource` Bean 추가, Vercel 도메인 및 로컬 개발 환경 origin 허용

---

### 계약 목록 조회 수 초 지연 (N+1 쿼리)

**문제**: 계약 목록 페이지 로딩이 수 초씩 걸림  
**원인**: N+1 쿼리 문제. `Contract` 엔티티가 `Unit`을 `LAZY` 로딩하는데, `ContractResponse.from()`에서 `unit.getId()`, `unit.getUnitNumber()`를 접근하면서 계약 1건당 Unit SELECT 쿼리가 추가 발생

```
페이지당 20건 기준: 1(목록) + 1(COUNT) + 20(Unit 개별 조회) = 22+ 쿼리
```

**해결**: `ContractRepository` 4개 쿼리에 `JOIN FETCH c.unit` 추가

```java
// 변경 전
@Query("SELECT c FROM Contract c WHERE ...")

// 변경 후 (페이지네이션)
@Query(value = "SELECT c FROM Contract c JOIN FETCH c.unit WHERE ...",
       countQuery = "SELECT COUNT(c) FROM Contract c WHERE ...")

// 변경 후 (List 반환)
@Query("SELECT c FROM Contract c JOIN FETCH c.unit WHERE ...")
```

> 페이지네이션 쿼리는 `JOIN FETCH`와 자동 생성 COUNT 쿼리가 충돌하므로 `countQuery`를 반드시 별도 지정해야 함.

**개선 결과**: 페이지당 22+ 쿼리 → 2 쿼리 (JOIN 1 + COUNT 1)
