# Polly Brain

Polly's AI backend runs as a Vercel serverless function at `/api/chat`.

The OpenAI API key is stored only in Vercel as `OPENAI_API_KEY`; it must never be committed to this repository or embedded in the Android APK.

The Android app will call the backend over HTTPS.
