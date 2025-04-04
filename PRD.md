# Product Requirements Document (PRD)

## 1. Genel Bilgiler
**Proje Adı:** Kurumsal E-Ticaret Sistemi (Backend API - Spring Boot)  
**Tarih:** 2025  
**Teknoloji Stack:** Spring Boot 3.x, Java 21, PostgreSQL, Redis, Kafka, Docker, Kubernetes, GraphQL, OpenAPI 3.0, OAuth2, Keycloak  
**API Türü:** RESTful & GraphQL Hybrid  

---

## 2. Amaç
Bu doküman, Kurumsal E-Ticaret Sisteminin Backend API gereksinimlerini belirlemek amacıyla hazırlanmıştır. API, müşteri, ürün, sipariş ve ödeme süreçlerini yönetmek için geliştirilecektir. Aynı zamanda CMS ile entegre çalışacaktır.

---

## 3. API Gereksinimleri
### 3.1 Kimlik Doğrulama ve Yetkilendirme
Kimlik doğrulama ve yetkilendirme işlemleri için **OAuth2** ve **JWT (JSON Web Token)** tabanlı bir mekanizma kullanılacaktır. **Keycloak**, kullanıcı yönetimi ve rol bazlı erişim kontrolü sağlamak için entegre edilecektir. API güvenliğini artırmak için bir **API Gateway** kullanılacaktır. 

#### 3.1.1 OAuth2 ve JWT Tabanlı Authentication
- Kullanıcıların güvenli bir şekilde giriş yapmasını sağlamak için **OAuth2 Authorization Code Flow** kullanılacaktır.
- Erişim ve yenileme tokenları **JWT (JSON Web Token)** formatında olacak ve Redis üzerinde saklanacaktır.
- JWT tokenlarında **kullanıcı rolü, yetkilendirme kapsamı ve oturum süresi** bilgileri yer alacaktır.
- Expired tokenları yönetmek için **Refresh Token mekanizması** uygulanacaktır.
- OAuth2 Scopes yapısı kullanılarak **kullanıcı yetkilerine göre API erişimleri kısıtlanacaktır.**

#### 3.1.2 Keycloak ile Kullanıcı ve Rol Yönetimi
- Kullanıcı yönetimi için **Keycloak Identity & Access Management** sistemi kullanılacaktır.
- Kullanıcılar, **email ve şifre**, **Google OAuth**, **Facebook OAuth** gibi yöntemlerle giriş yapabilecektir.
- Rol bazlı erişim yönetimi (**RBAC - Role-Based Access Control**) uygulanacaktır:
  - **Admin**: Tüm sistem yönetimine erişebilir.
  - **Satıcı (Vendor)**: Ürün ekleme, stok yönetimi gibi işlemleri yapabilir.
  - **Müşteri (Customer)**: Ürün satın alma ve sipariş yönetimi yapabilir.
- OTP (One-Time Password) ve **2FA (Two-Factor Authentication)** desteği sağlanacaktır.
- Kullanıcı etkinlikleri loglanacak ve **audit log sistemi** ile takip edilecektir.

#### 3.1.3 API Gateway ile Erişim Kontrolü
- **Spring Cloud Gateway** veya **Kong API Gateway** kullanılarak erişim kontrolü sağlanacaktır.
- Tüm API istekleri, API Gateway üzerinden geçerek **yetkilendirme ve rate-limiting** kontrollerine tabi tutulacaktır.
- Rate-limiting ile **DDoS saldırılarına karşı koruma sağlanacaktır.**
- API loglama ve izleme için **Prometheus ve Grafana** entegrasyonu yapılacaktır.
- Güvenli endpointler için **mTLS (Mutual TLS)** desteği sunulacaktır.

### 3.2 Kullanıcı Yönetimi
- Kullanıcı kayıt, giriş ve şifre yönetimi
- Roller: **Admin, Satıcı, Müşteri**
- İki faktörlü kimlik doğrulama (2FA)
- Kullanıcı etkinlik takibi (Audit Log)

### 3.3 Ürün ve Kategori Yönetimi
- Ürün ekleme, güncelleme, silme
- Kategori bazlı filtreleme
- Stok yönetimi
- Elasticsearch ile ürün arama ve filtreleme

### 3.4 Sipariş Yönetimi
- Sepet yönetimi
- Sipariş oluşturma ve ödeme işlemleri
- Sipariş durumu güncellemeleri
- Fatura ve e-arşiv entegrasyonu

### 3.5 Ödeme Entegrasyonu
- Stripe, iyzico, PayPal entegrasyonu
- 3D Secure ödeme desteği
- Abonelik bazlı ödeme modelleri

### 3.6 CMS İçeriği Yönetimi
- Blog, duyuru ve kampanya yönetimi
- SEO optimizasyonu
- İçerik düzenleme API’ları

### 3.7 Raporlama ve Analitik
- Kullanıcı ve sipariş istatistikleri
- Grafana ve Prometheus entegrasyonu
- Gerçek zamanlı dashboard

### 3.8 Bildirim ve E-Posta Yönetimi
- Kafka tabanlı event-driven bildirim altyapısı
- E-posta, SMS ve push bildirim entegrasyonu
- WebSocket ile gerçek zamanlı güncellemeler

---

## 4. Teknolojik Altyapı
- **Spring Boot 3.x** (Microservices yapısı)
- **Java 21** (Virtual Threads, Record, Pattern Matching)
- **Spring Security & OAuth2** (Kimlik doğrulama ve yetkilendirme)
- **PostgreSQL + Redis** (Veri ve cache yönetimi)
- **Kafka & RabbitMQ** (Event-Driven Architecture)
- **Elasticsearch** (Ürün ve sipariş arama motoru)
- **GraphQL** (Esnek ve optimize veri çekme)
- **OpenAPI 3.0** (API dökümantasyonu)
- **Docker & Kubernetes** (Containerization ve scaling)
- **CI/CD (GitHub Actions, ArgoCD, Jenkins)**

---

## 5. Performans ve Güvenlik Gereksinimleri
- 10.000 eşzamanlı kullanıcı desteği
- DDoS ve SQL Injection koruması
- Rate Limiting & API Throttling
- Günlük backup ve veri yedekleme politikaları
- GDPR & KVKK uyumluluğu

---

## 6. Geliştirme Yol Haritası
1. **Sprint 1:** Kullanıcı yönetimi ve authentication (4 Hafta)
2. **Sprint 2:** Ürün ve sipariş yönetimi (5 Hafta)
3. **Sprint 3:** Ödeme entegrasyonu ve CMS API (4 Hafta)
4. **Sprint 4:** Bildirimler, loglama ve monitoring (3 Hafta)
5. **Sprint 5:** Performans testleri ve güvenlik kontrolleri (3 Hafta)
6. **Sprint 6:** API dokümantasyonu ve production release (2 Hafta)

---

## 7. Sonuç
Bu PRD, e-ticaret projesinin backend API geliştirme sürecinin tüm teknik gereksinimlerini belirlemektedir. Modern ve ölçeklenebilir bir yapı hedeflenmiştir. API’nin geliştirme süreci, güvenlik önlemleri ve teknolojik altyapı en son teknolojilere uygun şekilde tasarlanmıştır. Geliştirme adımları tamamlandığında, sistemin yüksek performanslı ve güvenli bir şekilde çalışması sağlanacaktır.

