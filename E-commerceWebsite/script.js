let currentIndex = 0;
const slides = document.querySelectorAll('.banner-slide');
const totalSlides = slides.length;

function showNextSlide() {
  currentIndex = (currentIndex + 1) % totalSlides;
  const offset = -currentIndex * 100;
  slides.forEach((slide) => {
    slide.style.transform = `translateX(${offset}%)`;
  });
}

setInterval(showNextSlide, 3000); // Change slide every 3 seconds


document.getElementById('loginBtn').addEventListener('click', function() {
    // Display the signup form
    document.getElementById('signup-form').style.display = 'block';
  });
  