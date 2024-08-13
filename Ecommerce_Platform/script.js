document.addEventListener('DOMContentLoaded', function() {
  let currentIndex = 0;
  const slides = document.querySelectorAll('.banner-slide');
  const totalSlides = slides.length;
  const carouselInner = document.querySelector('.carousel-inner');

  function showNextSlide() {
      currentIndex++;
      
      if (currentIndex >= totalSlides) {
          // Reset to the first slide immediately after the last one
          carouselInner.style.transition = 'none';
          carouselInner.style.transform = 'translateX(0%)';
          currentIndex = 0;
          // Force a reflow to ensure the transition can restart
          carouselInner.offsetHeight; // Trigger reflow
          setTimeout(() => {
              carouselInner.style.transition = 'transform 0.5s ease-in-out';
              carouselInner.style.transform = `translateX(-${currentIndex * 100}%)`;
          }, 20); // Short delay to ensure the reflow has happened
      } else {
          carouselInner.style.transform = `translateX(-${currentIndex * 100}%)`;
      }
  }

  setInterval(showNextSlide, 3000); // Change slide every 3 seconds
});
