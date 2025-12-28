# DepremSafe - Deprem Güvenlik Uygulaması

## 🚀 Kurulum

1. **Projeyi Android Studio ile açın**
2. Android Studio otomatik olarak Gradle sync yapacak
3. İlk açılışta dependencies indirme 2-3 dakika sürebilir
4. **RetrofitClient.kt** dosyasında backend URL'inizi güncelleyin
5. Run ▶️ butonuna basın

## ⚙️ Eğer "gradle-wrapper.jar not found" Hatası Alırsanız

Android Studio otomatik olarak düzeltecektir:
- Alt tarafta "Download gradle-wrapper.jar" mesajı çıkacak
- **Download** veya **OK** butonuna tıklayın
- Android Studio dosyayı otomatik indirecek

## 📱 Özellikler

- 4 sayfalık onboarding
- Hızlı durum bildirimi (Güvendeyim / Yardım İhtiyacım Var)
- AI chatbot desteği
- Deprem hazırlık rehberi

## 🔧 Gereksinimler

- Android Studio Arctic Fox veya üzeri
- JDK 17+
- Android SDK 34

## 📝 Backend URL Değiştirme

`app/src/main/java/com/example/depremsafe/data/api/RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://your-backend-url.com/"
```

Başarılar! 🎉
