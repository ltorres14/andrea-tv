# Auditoría y contexto maestro — Andrea TV

**Fecha de corte:** 29 de septiembre de 2026  
**Proyecto local:** `C:\projects\andrea-tv`  
**Repositorio:** `https://github.com/ltorres14/andrea-tv.git`  
**SSH:** `git@github.com:ltorres14/andrea-tv.git`  
**Package Android:** `com.andreatv`

---

## 1. Objetivo del proyecto

Andrea TV es una aplicación Android TV nativa enfocada inicialmente en **películas y series VOD**.

En esta primera etapa **no se integrará TV en vivo**.

La intención es consumir directamente un servicio IPTV/Xtream autorizado, evitando depender de aplicaciones intermediarias como Xuper para presentar el catálogo y reproducir contenido.

La prioridad técnica es que la aplicación funcione correctamente en hardware de bajos recursos, particularmente en el TV Box TX2 disponible para pruebas.

---

## 2. Hardware objetivo principal

Dispositivo probado: **TX2 TV Box**

Datos obtenidos mediante ADB:

- IP actual usada para desarrollo: `192.168.1.6:5555`
- Android: **6.0.1**
- SDK/API: **23**
- ABI principal: **armeabi-v7a**
- ABI list: `armeabi-v7a, armeabi`
- Arquitectura práctica: **32 bits**
- RAM total: aproximadamente **1 GB**
- Memoria total reportada: `1020296 kB`
- RAM disponible observada: aproximadamente `387829 kB`
- RAM usada observada: aproximadamente `481361 kB`
- Lost RAM observada: aproximadamente `151106 kB`
- Almacenamiento `/data`: aproximadamente **12.4 GB**
- Espacio libre observado: aproximadamente **11.1 GB**
- Resolución física: **1280×720**
- Densidad: **160 dpi**

### Consecuencia técnica

El almacenamiento no es actualmente el cuello de botella.

La prioridad es controlar:

- RAM
- carga de imágenes
- número de objetos vivos
- cantidad de elementos renderizados
- cachés
- prefetch
- reproducción
- procesamiento de catálogo

Andrea TV debe comportarse como una aplicación optimizada para TV de gama baja.

---

## 3. Investigación IPTV previa

Se realizó previamente una investigación técnica sobre Xuper / Xuper Hydra / Xuper Mobile NF y el servicio IPTV utilizado.

Conclusiones relevantes para Andrea TV:

- El servicio utiliza un panel Xtream-style.
- El panel visible probado fue `alltvpremiere.online:8000`.
- Se observaron múltiples nodos backend para servir contenido.
- Los streams VOD pueden terminar en URLs backend con token.
- Se confirmó que un token VOD probado podía reutilizarse en múltiples solicitudes.
- Posteriormente se observó que una URL con token seguía funcionando incluso después de varios días.
- Aun así, Andrea TV **no debe diseñarse dependiendo de reutilizar tokens antiguos**.
- La reproducción debe partir del flujo normal autorizado del panel.
- El servidor reportó `max_connections = 4` para la cuenta probada.
- Una app propia no elimina ese límite del proveedor.

Endpoints Xtream relevantes:

```text
player_api.php
?action=get_vod_categories
?action=get_vod_streams
?action=get_series_categories
?action=get_series
```

Ejemplo conceptual:

```text
Panel IPTV
   ↓
autenticación
   ↓
categorías
   ↓
catálogo VOD / series
   ↓
stream_id
   ↓
URL autorizada de reproducción
   ↓
redirección / token / backend
   ↓
reproductor
```

---

## 4. Decisión sobre paginación

Se asume que el servidor **no ofrece paginación útil del catálogo**.

Por lo tanto:

```text
Servidor
   ↓
catálogo completo
   ↓
procesamiento progresivo
   ↓
Room
   ↓
Paging local
   ↓
RecyclerView
```

No se debe confundir:

- paginación visual
- paginación local
- paginación de red

El servidor puede entregar miles de registros en una sola respuesta, pero Andrea TV no debe mantener todo el catálogo en memoria durante la navegación.

---

## 5. Estrategia de persistencia

Se decidió utilizar **Room** como base de datos local.

Room será utilizado para almacenar metadata ligera del catálogo:

- `stream_id`
- nombre
- categoría
- URL de póster
- extensión
- rating
- año
- otros metadatos necesarios

No se almacenarán las películas completas en Room.

Modelo conceptual:

```text
Servidor IPTV
   ↓
sincronización
   ↓
Room / SQLite
   ↓
Paging
   ↓
UI
```

La primera sincronización deberá:

1. recibir la respuesta del servidor;
2. procesarla progresivamente;
3. insertar por lotes;
4. liberar cada lote;
5. marcar sincronización;
6. permitir que la UI consulte Room, no el JSON completo.

Tamaño de lote inicial considerado:

```text
100–200 registros
```

Página lógica inicial considerada:

```text
24 elementos
```

Estos números deberán ajustarse con pruebas reales.

---

## 6. Stack técnico decidido

Primera versión:

- **Android Studio**
- **Android TV nativo**
- **Kotlin**
- **Views/XML**
- **RecyclerView**
- **Room**
- **Paging 3**
- **Media3 / ExoPlayer**
- `minSdk 23`
- soporte prioritario `armeabi-v7a`
- orientación landscape

No se usará Compose en esta primera versión.

Motivos principales:

- compatibilidad con Android 6.0.1;
- hardware con ~1 GB de RAM;
- control fino de vistas;
- control remoto / D-pad;
- minimizar complejidad y consumo.

---

## 7. Estructura de paquetes acordada

Estado base:

```text
com.andreatv
├── data
├── ui
│   └── home
│       ├── MainActivity.kt
│       ├── adapter
│       └── model
└── util
```

Arquitectura futura prevista:

```text
data/
    api/
    database/
        dao/
        entity/
    repository/

ui/
    home/
    movies/
    series/
    detail/
    player/
    search/

sync/

model/

util/
```

Principio acordado:

```text
DTO ≠ Entity ≠ Model
```

- DTO = formato recibido del servidor
- Entity = formato almacenado en Room
- Model = formato usado por la UI

---

## 8. Creación inicial del proyecto

Plantilla elegida:

**Android TV Blank Views Activity**

Configuración inicial:

- Name: `Andrea TV`
- Package: `com.andreatv`
- Save location: `C:\projects\andrea-tv`
- Language: Kotlin
- Minimum SDK: API 23
- Build configuration: Kotlin DSL

La plantilla inicial generó código demo de Leanback con múltiples archivos que no eran necesarios.

Archivos demo identificados:

```text
BrowseErrorActivity.kt
CardPresenter.kt
DetailsActivity.kt
DetailsDescriptionPresenter.kt
ErrorFragment.kt
MainFragment.kt
Movie.kt
MovieList.kt
PlaybackActivity.kt
PlaybackVideoFragment.kt
VideoDetailsFragment.kt
```

Estos fueron eliminados para dejar una base limpia.

---

## 9. Problemas de compilación encontrados

### 9.1 Código demo Leanback

La plantilla generó incompatibilidades en clases como:

- `CardPresenter.kt`
- `MainFragment.kt`
- `VideoDetailsFragment.kt`

Se decidió no invertir tiempo reparando la demo porque no forma parte de Andrea TV.

### 9.2 AndroidX Core incompatible

El proyecto fue generado con:

```text
Android Gradle Plugin 9.0.1
compileSdk 36
```

pero recibió:

```text
androidx.core:core-ktx:1.19.0
```

La dependencia exigía:

- compileSdk 37
- AGP 9.1.0+

Se corrigió bajando Core KTX a una versión compatible:

```text
1.17.0
```

### 9.3 BOM en libs.versions.toml

PowerShell guardó accidentalmente `libs.versions.toml` con UTF-8 BOM.

Gradle reportó:

```text
Unexpected '\ufeff'
```

Se corrigió guardando el archivo como UTF-8 sin BOM.

Resultado:

```text
BUILD SUCCESSFUL
```

---

## 10. Reorganización de MainActivity

Se movió:

```text
com.andreatv.MainActivity
```

a:

```text
com.andreatv.ui.home.MainActivity
```

Ruta final:

```text
C:\projects\andrea-tv\app\src\main\java\com\andreatv\ui\home\MainActivity.kt
```

El `AndroidManifest.xml` fue actualizado para apuntar a:

```text
.ui.home.MainActivity
```

Se crearon también:

```text
com.andreatv.data
com.andreatv.util
```

Un intento inicial de automatizar el movimiento usó `$home`, que en PowerShell colisionó con la variable reservada `$HOME`.

Se corrigió posteriormente usando `$homeDir`.

El estado final fue validado mediante:

```text
BUILD SUCCESSFUL
```

---

## 11. Primera prueba real en TX2

Se instaló y ejecutó la versión mínima de Andrea TV en el TX2.

Pantalla inicial mostrada correctamente:

```text
ANDREA TV
Primera ejecución
```

Esto validó:

- compilación;
- instalación;
- ejecución;
- compatibilidad básica con Android 6.0.1;
- compatibilidad con API 23;
- funcionamiento en hardware real.

---

## 12. Diseño visual acordado

Referencia visual general:

- aplicación de streaming;
- fondo muy oscuro;
- acento amarillo/naranja;
- tarjetas verticales;
- carruseles horizontales;
- interfaz limpia;
- fuerte adaptación a control remoto.

Paleta base:

```text
Fondo principal:  #0E1120
Superficies:      #161A2B
Acento:           #FFAF2E
Texto principal:  #FFFFFF
Texto secundario: gris
```

Principios:

- no parecer una lista IPTV tradicional;
- parecer una plataforma moderna de streaming;
- navegación evidente por foco;
- nada de efectos pesados;
- evitar blur en tiempo real;
- evitar fondos gigantes;
- evitar previews automáticos de video;
- animaciones mínimas.

Foco D-pad:

```text
borde amarillo
escala aproximada 1.05x
```

---

## 13. Logo oficial elegido

Nombre de marca:

**Andrea TV**

Se descartaron diseños con:

- ícono grande de play;
- televisor;
- frases promocionales;
- demasiados elementos visuales.

Dirección final:

- logotipo `AndreaTV`;
- “Andrea” blanco/plata;
- “TV” amarillo/naranja;
- acento amarillo cruzando la `A`;
- icono de palomitas asociado al cine;
- fondo azul muy oscuro / negro;
- formato cuadrado 1:1;
- sin marco amarillo exterior.

Este diseño se considera el logo oficial de esta etapa.

---

## 14. Primera Home implementada por Codex

Codex terminó la primera etapa visual.

### Funciones incluidas

- encabezado “Andrea TV”;
- búsqueda;
- configuración;
- banner destacado;
- metadatos ficticios;
- botón “Reproducir”;
- carrusel “Películas populares”;
- carrusel “Series”;
- aproximadamente seis tarjetas visibles a 1280×720;
- navegación D-pad;
- borde amarillo de foco;
- escala 1.05 al enfocar;
- drawables XML ligeros;
- datos mock;
- sin networking;
- sin dependencias nuevas.

### Kotlin

Modificado:

```text
app/src/main/java/com/andreatv/ui/home/MainActivity.kt
```

Creado:

```text
app/src/main/java/com/andreatv/ui/home/model/Poster.kt
app/src/main/java/com/andreatv/ui/home/adapter/PosterAdapter.kt
```

### Layouts / values

Modificado:

```text
app/src/main/res/layout/activity_main.xml
```

Creado:

```text
app/src/main/res/layout/item_poster.xml
```

Modificados:

```text
colors.xml
strings.xml
themes.xml
```

### Drawables creados

```text
bg_featured.xml
bg_icon_action.xml
bg_play_button.xml
bg_poster_focus.xml

ic_search.xml
ic_settings.xml
ic_play.xml

poster_*.xml
```

Se crearon siete placeholders de póster.

### Build de Codex

Resultado:

```text
BUILD SUCCESSFUL in 25s
33 actionable tasks: 33 up-to-date
```

APK:

```text
C:\projects\andrea-tv\app\build\outputs\apk\debug\app-debug.apk
```

Codex no hizo commit ni push.

---

## 15. Qué NO se ha implementado todavía

A la fecha de esta auditoría todavía no se debe considerar implementado:

- API IPTV real;
- autenticación real;
- Room;
- Paging 3 real;
- sincronización del catálogo;
- Retrofit/OkHttp;
- Media3/ExoPlayer;
- reproducción de películas;
- Series reales;
- temporadas;
- episodios;
- favoritos persistentes;
- búsqueda real;
- configuración real;
- almacenamiento seguro de credenciales;
- mediciones finales de RAM;
- caché de imágenes;
- sistema de actualización;
- TV en vivo.

---

## 16. Flujo futuro esperado

### Películas

```text
Inicio
  ↓
Películas
  ↓
categorías
  ↓
catálogo
  ↓
detalle
  ↓
reproducir
```

### Series

```text
Inicio
  ↓
Series
  ↓
serie
  ↓
temporada
  ↓
episodio
  ↓
reproducir
```

### Datos

```text
API
 ↓
DTO
 ↓
sincronización
 ↓
Room Entity
 ↓
Repository
 ↓
Paging
 ↓
UI Model
 ↓
RecyclerView
```

---

## 17. Estrategia de memoria

Meta conceptual durante navegación normal:

```text
aprox. 100–150 MB
```

Evitar acercarse innecesariamente a:

```text
200 MB+
```

Reglas:

- una sola instancia de reproductor;
- liberar reproductor al salir;
- no precargar películas completas;
- no guardar imágenes grandes en RAM;
- reducir pósters al tamaño real de la tarjeta;
- caché de imágenes limitada;
- pocos elementos activos por pantalla;
- procesamiento progresivo de JSON;
- catálogo completo en disco, no en RAM;
- Room como fuente local;
- Paging para ventanas pequeñas.

---

## 18. Reglas de trabajo del proyecto

1. Trabajar principalmente desde Android Studio.
2. Usar PowerShell sólo para:
   - ADB;
   - diagnósticos;
   - scripts puntuales;
   - reorganizaciones masivas.
3. Probar frecuentemente en el TX2 real.
4. Evitar cambios grandes sin validación.
5. No mezclar varias etapas funcionales al mismo tiempo.
6. Mantener `minSdk 23`.
7. Mantener `com.andreatv`.
8. No migrar a Compose en esta etapa.
9. Evitar dependencias innecesarias.
10. No hacer commit/push automático durante fases de prueba sin revisión.
11. Priorizar compatibilidad, RAM y navegación D-pad sobre efectos visuales.
12. TV en vivo queda fuera del alcance inicial.

---

## 19. Siguiente etapa recomendada

Antes de conectar API real, validar visualmente la Home de Codex en el TX2:

- instalar/ejecutar desde Android Studio;
- revisar proporciones a 1280×720;
- probar D-pad;
- verificar foco;
- comprobar que los carruseles navegan correctamente;
- comprobar que ningún elemento queda inaccesible;
- medir RAM en navegación básica.

Si la Home pasa esa prueba, la siguiente etapa puede ser:

**conectar la API solamente para lectura del catálogo y categorías**, sin reproducción todavía.

Después:

1. API;
2. sincronización;
3. Room;
4. Paging;
5. pósters reales;
6. detalle;
7. reproductor;
8. series.

---

## 20. Estado actual resumido

```text
Nombre: Andrea TV
Plataforma: Android TV
Lenguaje: Kotlin
UI: Views/XML
minSdk: 23
Objetivo principal: TX2
Android objetivo probado: 6.0.1
RAM objetivo: ~1 GB
Resolución: 1280×720
ABI: armeabi-v7a
Home mock: implementada
Build: exitoso
API: pendiente
Room: pendiente
Paging: pendiente
Media3: pendiente
Series reales: pendiente
TV en vivo: fuera de alcance
Logo: definido
Repositorio: creado
```

---

## 21. Fuente de verdad para futuras conversaciones

Este archivo debe utilizarse como contexto base cuando se abra una conversación nueva sobre Andrea TV.

Si existe discrepancia entre este documento y el estado real del código, debe prevalecer:

1. el código actual;
2. el build actual;
3. las pruebas en TX2;
4. esta auditoría como contexto histórico y de decisiones.

Cada etapa importante debería actualizar este documento o generar una nueva versión de auditoría.
