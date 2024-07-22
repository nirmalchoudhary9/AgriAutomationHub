function checkSoilFertility() {
    // Get input values
    var nitrogen = parseFloat(document.getElementById('nitrogen').value);
    var phosphorus = parseFloat(document.getElementById('phosphorus').value);
    var potassium = parseFloat(document.getElementById('potassium').value);
    var ph = parseFloat(document.getElementById('ph').value);

    // Check if values are in the desired range
    if (nitrogen >= 20 && nitrogen <= 50 &&
        phosphorus >= 20 && phosphorus <= 50 &&
        potassium >= 150 && potassium <= 300 &&
        ph >= 6 && ph <= 8.5) {

        // Normalize pH to a 0-1 scale
        var normalizedPh = (ph - 6) / (8.5 - 6);

        // Apply weights to each nutrient
        var weightNitrogen = 0.4;
        var weightPhosphorus = 0.3;
        var weightPotassium = 0.2;
        var weightPh = 0.1;

        // Calculate soil fertility
        var soilFertility = (
            (nitrogen - 20) / (50 - 20) * weightNitrogen +
            (phosphorus - 20) / (50 - 20) * weightPhosphorus +
            (potassium - 150) / (300 - 150) * weightPotassium +
            normalizedPh * weightPh
        ) * 100;

        // Display the result
        var resultElement = document.getElementById('result');
        resultElement.innerHTML = 'Soil Fertility: ' + soilFertility.toFixed(2) + '%';

        // Check fertility level
        if (soilFertility >= 75) {
            resultElement.style.color = '#27ae60'; // Green for high fertility
        } else if (soilFertility >= 50) {
            resultElement.style.color = '#f39c12'; // Orange for moderate fertility
        } else {
            resultElement.style.color = '#e74c3c'; // Red for low fertility
        }

    } else {
        // Display an error message for invalid input values
        alert('Please enter valid values in the specified range.');
    }
}
