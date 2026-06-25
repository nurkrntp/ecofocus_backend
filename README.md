# EcoFocus Backend Kurulum Kılavuzu

## Gereksinimler

Projeyi çalıştırmadan önce aşağıdaki araçların kurulu olduğundan emin olun.

| Araç | Minimum Sürüm |
|------|---------------|
| Java JDK | 21+ |
| Maven | 3.8+ |
| PostgreSQL | 14+ |

---

## 1. Veritabanını Oluşturun

PostgreSQL üzerinde aşağıdaki komutu çalıştırarak veritabanını oluşturun.

```sql
CREATE DATABASE ecofocus;
```

Varsayılan kullanıcı bilgileri:

- **Kullanıcı Adı:** `postgres`
- **Şifre:** `postgres`

Farklı bir kullanıcı adı veya şifre kullanıyorsanız bir sonraki adımda gerekli güncellemeleri yapın.

---

## 2. `application.properties` Dosyasını Yapılandırın

`src/main/resources/application.properties` dosyasını açın ve veritabanı bağlantı bilgilerini kontrol edin.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecofocus
spring.datasource.username=postgres
spring.datasource.password=postgres
```

PostgreSQL kullanıcı bilgileriniz farklıysa yalnızca bu satırları güncellemeniz yeterlidir. Diğer yapılandırmaları değiştirmenize gerek yoktur.

---

## 3. Resend API Anahtarını Ayarlayın

Proje kök dizininde `.env` adlı bir dosya oluşturun (veya mevcut dosyayı açın) ve aşağıdaki satırı ekleyin.

```env
RESEND_API_KEY=your_resend_api_key_here
```

Resend API anahtarınızı ücretsiz olarak **https://resend.com** üzerinden oluşturabilirsiniz.

> **Not:** E-posta doğrulama ve şifre sıfırlama işlemleri bu API anahtarı olmadan çalışmaz.

---

## 4. Projeyi Derleyin ve Çalıştırın

Önce projeyi derleyin:

```bash
mvn clean install
```

Ardından Spring Boot uygulamasını başlatın:

```bash
mvn spring-boot:run
```

Backend başarıyla başlatıldığında aşağıdaki adreste çalışacaktır.

```
http://localhost:8080
```

---

## Sık Karşılaşılan Sorunlar

| Sorun | Çözüm |
|-------|-------|
| PostgreSQL bağlantı hatası | PostgreSQL servisinin çalıştığını ve `application.properties` dosyasındaki bağlantı bilgilerinin doğru olduğunu kontrol edin. |
| `RESEND_API_KEY` hatası | `.env` dosyasının proje kök dizininde bulunduğundan ve API anahtarının doğru tanımlandığından emin olun. |
| Port çakışması | `application.properties` dosyasındaki `server.port=8080` değerini kullanılmayan başka bir port ile değiştirin. |