# Wordle Multijugador

Un juego de Wordle en consola con arquitectura Cliente-Servidor, desarrollado en **Kotlin**. Permite a varios jugadores conectarse simultáneamente a un servidor para jugar sus partidas.

## 🚀 Características

- **Arquitectura Cliente-Servidor:** Comunicación mediante Sockets TCP.
- **Concurrencia:** Uso de Corrutinas de Kotlin (`kotlinx.coroutines`) para gestionar de forma eficiente múltiples clientes simultáneamente.
- **Serialización de Mensajes:** Intercambio de información entre cliente y servidor mediante formato JSON usando **Gson**.
- **Gestión de Jugadores:** El servidor controla el límite máximo de clientes conectados de forma simultánea.
- **Interfaz por Consola:** Menú principal interactivo en el cliente.
- **Sistema de Récords:** Persistencia de las puntuaciones en el servidor (`records.json`).

## 🛠️ Tecnologías utilizadas

- **Lenguaje:** Kotlin (JVM 1.8)
- **Gestor de dependencias:** Maven
- **Librerías principales:**
  - `kotlinx-coroutines-core` (Asincronía y concurrencia)
  - `gson` (Manejo de JSON)
  - `junit-jupiter` y `kotlin-test-junit5` (Pruebas unitarias)

## 📁 Estructura del Proyecto

El código está dividido en dos aplicaciones principales dentro del paquete `src/main/kotlin/`:

- `client/`: Lógica del jugador.
  - `ClientMain.kt`: Punto de entrada del cliente. Se conecta al servidor en `localhost:5678`.
- `server/`: Lógica del servidor (gestión de red, salas, diccionario y récords).
  - `ServerMain.kt`: Punto de entrada del servidor.

## ⚙️ Requisitos previos

- **Java Development Kit (JDK):** Versión 8 o superior.
- **Maven:** Para descargar las dependencias y compilar el proyecto.

## 🚀 Cómo ejecutar el proyecto

### 1. Compilar el proyecto

Abre una terminal en la carpeta `Wordle` del proyecto y ejecuta:

```bash
mvn clean install
```

### 2. Iniciar el Servidor

Ejecuta la clase principal del servidor:
* Desde tu IDE (IntelliJ IDEA, Eclipse, etc.) ejecutando `ServerMain.kt`.
* O usando Maven (si configuras el plugin de ejecución).

Verás el mensaje de que el servidor está a la escucha de clientes.

### 3. Iniciar un Cliente

Abre una nueva terminal (o ejecuta desde el IDE) la clase principal del cliente (`ClientMain.kt`).
1. El juego te pedirá tu nombre de usuario.
2. Se conectará automáticamente al servidor.
3. ¡Sigue las instrucciones del menú para jugar a Wordle!

*(Puedes ejecutar varias instancias de `ClientMain.kt` para simular múltiples jugadores conectados a la vez).*
