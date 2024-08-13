import { getAuth, signInWithEmailAndPassword, updateProfile } from 'https://www.gstatic.com/firebasejs/10.11.1/firebase-auth.js';
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.11.1/firebase-app.js';
import { getAnalytics } from 'https://www.gstatic.com/firebasejs/10.11.1/firebase-analytics.js';

// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyA-YeMsoObv2ybpy-W6BDJJJHQNhxAksKs",
    authDomain: "agriautomationhub-f3ceb.firebaseapp.com",
    databaseURL: "https://agriautomationhub-f3ceb-default-rtdb.firebaseio.com",
    projectId: "agriautomationhub-f3ceb",
    storageBucket: "agriautomationhub-f3ceb.appspot.com",
    messagingSenderId: "191351033464",
    appId: "1:191351033464:web:b90b6ec5ea4f48e248278d",
    measurementId: "G-DC4PSFERPT"
};

const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);

const auth = getAuth(app);

document.addEventListener("DOMContentLoaded", function() {
    const loginForm = document.getElementById('loginForm');
    
    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();
        
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const displayName = document.getElementById('user-name').value; // Set the display name here or get it from input

        signInWithEmailAndPassword(auth, email, password)
            .then((userCredential) => {
                const user = userCredential.user;
                // Ensure displayName is a string and not empty
                if (displayName && typeof displayName === 'string' && displayName.trim() !== "") {
                    updateProfile(user, { displayName: displayName })
                        .then(() => {
                            window.location.href = 'e.html';
                        })
                        .catch((error) => {
                            console.error('Error updating profile', error);
                        });
                } else {
                    window.location.href = 'e.html';
                }
            })
            .catch((error) => {
                console.error('Error signing in', error);
            });
    });
});
