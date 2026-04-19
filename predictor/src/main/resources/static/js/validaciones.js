document.addEventListener('DOMContentLoaded', function() {
    const selectProvincia = document.getElementById('provincia');
    const selectZona = document.getElementById('zona');

    if (selectProvincia) {
        selectProvincia.addEventListener('change', function() {
            const provincia = this.value;

            if (!provincia) {
                selectZona.innerHTML = '<option value="">Primero elige provincia...</option>';
                return;
            }

            selectZona.innerHTML = '<option value="">Cargando barrios reales...</option>';

            fetch('/api/zonas?provincia=' + encodeURIComponent(provincia))
                .then(response => {
                    if (!response.ok) throw new Error("Fallo en el servidor");
                    return response.json();
                })
                .then(data => {
                    selectZona.innerHTML = '<option value="">Selecciona un barrio</option>';
                    // Verificamos que 'data' sea una lista
                    if (Array.isArray(data)) {
                        data.forEach(barrio => {
                            let option = document.createElement('option');
                            option.value = barrio;
                            option.text = barrio;
                            selectZona.appendChild(option);
                        });
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    selectZona.innerHTML = '<option value="">Error al cargar datos</option>';
                });
        });
    }
});