document.getElementById('predict-button').addEventListener('click', async function () {
    const fileInput = document.getElementById('file');
    const formData = new FormData();
    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
        try {
            const response = await fetch('http://127.0.0.1:5000/predict', {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();

            console.log('Response data:', data);

            const diseaseInfo = document.getElementById('disease-info');
            let cureInfo = document.getElementById('cure-info');
            const cure = document.getElementById('cure');

            diseaseInfo.textContent = `Disease: ${data.result}`;
            cure.textContent = "Cure: ";
            if(cureInfo.innerHTML != null){
                cureInfo.innerHTML = null;
            }
            if(data.cure == 1)
            {
                cureInfo.innerHTML += `<li>Relax!, No Cure Needed</li>`;
                cureInfo.innerHTML += `<li>The Plant is healthy</li>`;
            }
            else
            {
                for(let i = 0; i<(data.cure).length;i++)
                {
                    cureInfo.innerHTML += `<li>${data.cure[i]}</li>`;
                }
            }     

        } catch (error) {
            console.error('Fetch error:', error);

            const resultDiv = document.getElementById('result');
            resultDiv.innerHTML = `<h2>Predictions:</h2>`;
            resultDiv.innerHTML += `<p>Error: ${error.message}</p>`;
        }
    } else {
        console.error('No file selected');
        const resultDiv = document.getElementById('result');
        resultDiv.innerHTML = '<h2>Predictions:</h2>';
        resultDiv.innerHTML += '<p>Error: No file selected</p>';
    }

    // Prevent default form submission
    return false;
});