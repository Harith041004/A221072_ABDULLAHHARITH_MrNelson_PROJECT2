# 🌟 VitalityApp – SDG 3 Health & Well-being
A Comprehensive Health Ecosystem built with Jetpack Compose, Hardware Sensors, and Cloud Integration.

## 📖 Problem Statement
Many individuals struggle to maintain consistent fitness and mental health routines because they lack a centralized tool that connects their physical actions with community support. While professional ecosystems exist, they often lack localized motivation. 

VitalityApp bridges this gap by combining manual habit tracking, live hardware sensors to verify exercises, and cloud-based community motivation to create a holistic "Quantified Self" experience.

## 💡 Solution
VitalityApp is a modern Android application developed using **Jetpack Compose** that helps users track Movement, Nutrition, Sleep, and Mental Health. 

Expanding to a complete 7-screen flow, the app provides a platform where users can:
* **Monitor Vitality:** Get a real-time health score.
* **Verify Workouts:** Use the device camera and AI to automatically count push-ups.
* **Engage Locally & Globally:** Save workout history to the device and share milestones on a live cloud community board.
* **Reflect & Log:** Record daily moods and custom health targets.

## 🚀 This Project Demonstrates (Technical Pillars)
* **UI Expansion:** Multi-screen navigation (7 screens) using Navigation Compose and State Hoisting.
* **Hardware Sensors:** CameraX and Google ML Kit (Face Detection) for real-time push-up counting.
* **Local Persistence:** Room Database to save and retrieve offline user workout history.
* **Cloud Integration:** Firebase Firestore to read and write shared community posts remotely.
* **Data from the Internet:** Retrofit and Gson to fetch daily dynamic data from a free REST API (ZenQuotes).

## ✨ Features Included
* **Dynamic Home Dashboard:** Live vitality score calculation and quick-access top bar.
* **AI Practice Tracker:** Real-time exercise verification using the device camera.
* **Workout History:** Local offline log of past sessions (Room).
* **Community Board:** Live sync of user progress (Firebase) and a daily "Inspiration of the Day" quote (Retrofit/API).
* **Interactive Journal:** Habit sliders and reflection text fields.
* **Smart Goals System:** Real-time progress bars for custom targets.
* **Gamified Profile:** Achievement grid and day-streak tracking.

## 🌍 SDG Alignment: SDG 3 – Good Health and Well-being
This application encourages a holistic approach to health by making habit tracking accessible and intentional. By bridging physical activity with mental reflection and community support, it promotes long-term wellness for the Malaysian community.

