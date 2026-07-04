# ChatChat Firebase Project (Groovy Gradle)

This is the Groovy Gradle version of the ChatChat Android Studio project.

## What is Groovy here?
- The Gradle build files use Groovy DSL: `build.gradle`, `settings.gradle`, and `app/build.gradle`
- The Android app code is still Kotlin, which is normal for Android Studio projects

## Before you run
1. Create a Firebase project.
2. Register the Android app with package name: `com.example.chatchat`
3. Download the real `google-services.json`
4. Replace the placeholder file at `app/google-services.json`
5. In Firebase Console, enable:
   - Authentication -> Email/Password
   - Firestore Database -> Start in test mode
   - Storage
6. Open the project in Android Studio and let Gradle sync.
7. Run the app.

## Notes
- The included `app/google-services.json` is only a placeholder so the project structure is complete.
- Sign up / sign in use Firebase Authentication.
- User profiles and chat rooms/messages use Cloud Firestore.
- Profile image upload uses Firebase Storage.
- Call log screen is still sample UI data.

## Firestore structure
- `users/{uid}`
- `chatRooms/{roomId}`
- `chatRooms/{roomId}/messages/{messageId}`

## Recommended Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    match /chatRooms/{roomId} {
      allow read, write: if request.auth != null &&
        request.auth.uid in resource.data.participantIds;
      allow create: if request.auth != null &&
        request.auth.uid in request.resource.data.participantIds;

      match /messages/{messageId} {
        allow read, create: if request.auth != null &&
          request.auth.uid in get(/databases/$(database)/documents/chatRooms/$(roomId)).data.participantIds;
      }
    }
  }
}
```

## Recommended Storage Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{userId}.jpg {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
# Chat App (Kotlin + Firebase)

## Features
- User Authentication
- Real-time Chat
- Contacts
- Conversations
- Call Logs
- Firebase Integration

## Technologies Used
- Kotlin
- Android Studio
- Firebase Authentication
- Cloud Firestore
- XML
- Gradle

## Author
Tharusha Minidu
