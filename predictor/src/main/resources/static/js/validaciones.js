document.addEventListener('DOMContentLoaded', function() {
    const selectProvincia = document.getElementById('provincia');
    const selectZona = document.getElementById('zona');

    if (selectProvincia) {
        selectProvincia.addEventListener('change', function() {
            const provincia = this.value;

            if (!provincia) {
                // CORRECCIÓN BUENAS PRÁCTICAS: Eliminado innerHTML
                selectZona.options.length = 0;
                let opt = document.createElement('option');
                opt.value = "";
                opt.textContent = "Primero elige provincia...";
                selectZona.appendChild(opt);
                return;
            }

            // CORRECCIÓN BUENAS PRÁCTICAS: Cambiado por manipulación limpia de nodos
            selectZona.options.length = 0;
            let optCargando = document.createElement('option');
            optCargando.value = "";
            optCargando.textContent = "Cargando barrios reales...";
            selectZona.appendChild(optCargando);

            fetch('/api/zonas?provincia=' + encodeURIComponent(provincia))
                .then(response => {
                    if (!response.ok) throw new Error("Fallo en el servidor");
                    return response.json();
                })
                .then(data => {
                    selectZona.options.length = 0;
                    let optSelecciona = document.createElement('option');
                    optSelecciona.value = "";
                    optSelecciona.textContent = "Selecciona un barrio";
                    selectZona.appendChild(optSelecciona);

                    // Verificamos de forma segura que 'data' sea una lista
                    if (Array.isArray(data)) {
                        data.forEach(barrio => {
                            let option = document.createElement('option');
                            option.value = barrio;
                            option.textContent = barrio; // textContent es el estándar óptimo de seguridad XSS
                            selectZona.appendChild(option);
                        });
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    // CORRECCIÓN BUENAS PRÁCTICAS: Eliminado innerHTML en el bloque catch
                    selectZona.options.length = 0;
                    let optError = document.createElement('option');
                    optError.value = "";
                    optError.textContent = "Error al cargar datos";
                    selectZona.appendChild(optError);
                });
        });
    }
});