# SAYSYNSAY
*Proyecto Android desarrollado en Java*.

**Autora:** Paula Castillejo Bravo.

Se puede consultar el siguiente vídeo para ver una demostración del funcionamiento de la aplicación: [Vídeo de demostración](https://youtu.be/E48Ti1xFun4).

Se pueden consultar las capturas de pantalla de muestra de la aplicación desde la carpeta [Screenshot](./Screenshot/), ubicada en la raíz de proyecto.

---

## 1. Descripción general
**SaySynSay** es una aplicación Android desarrollada en Java que permite consultar información sobre palabras en inglés mediante una API externa. Su objetivo es facilitar el aprendizaje y la ampliación del vocabulario a través de la consulta a sinónimos, antonimos y pronunciaciones.

La aplicación permite a los usuarios buscar palabras, consultar su información y guardar aquellas que deseen conservar en su vocabulario personal. También registra las búsquedas realizadas y ofrece estadísticas sobre las palabras guardadas y su distribución por categorías gramaticales.

Además, incorpora un sistema de autenticación que permite acceder a las funcionalidades de la aplciación mediante distintos métodos de inicio de sesión.

## 2. Autenticación
### 2.1. Sistema inicial de autenticación
La aplicación incorpora una pantalla de inicio de sesión (`LoginActivity`) desde la que el usuario puede elegir entre tres métodos de acceso:

- **Correo electrónico y contraseña:** implementado mediante FirebaseUI Authentication, que proporciona la interfaz para registrarse e iniciar sesión.
- **Github:** también implementado mediante FirebaseUI Authentication, utilizando el proveedor OAuth de GitHub configurado previamente en Firebase.
- **Acceso anónimo:** implementado directamente mediante Firebase Authentication, utilizando `signInAnonymously()`.

Los dos primeros métodos utilizan FirebaseUI para gestionar la interfaz de autenticación, mientras que el acceso anónimo se realiza directamente mediante Firebase Authentication.

### 2.2. Conversión de cuenta anónima a cuenta permanente
Se incorpora la posibilidad de convertir una sesión anónima en una cuenta permanente mediante correo electrónico y contraseña o GitHub.

Esta funcionalidad se ofrece cuando un usuario anónimo intenta guardar una palabra en su vocabulario. Se muestra un diálogo informativo que permite acceder al proceso de actualización de la cuenta.

Durante este proceso, se oculta la opción de acceso anónimo y se habilita la actualización automática de cuentas anónimas mediante FirebaseUI (`enableAnonymousUserAutoUpgrade()`).

De este modo, las nuevas credenciales se vinculan a la cuenta anónima existente, permitiendo conservar su identidad en Firebase y mantener asociados los datos almacenados bajo su UID.

## 3. Conectividad con una API externa
La aplicación utiliza Free Dictionary API para obtener información sobre palabras en inglés. La comunicación se realiza mediante Retrofit, utilizando Gson para convertir las respuestas JSON a objetos Java de los modelos definidos a partir de la estructura de la API.

Las consultas se realizan al endpoint `entries/en/{word}` y permite obtener, entre otros datos:
- Definiciones y ejemplos de uso.
- Sinónimos y antónimos.
- Categorías gramaticales y formas de las palabras.
- Transcripciones fonéticas.

Las peticiones se ejecutan de forma asíncrona, evitando bloquear el hilo principal de la aplicación. Se contemplan las respuestas de error, incluyendo el caso en que la palabra no se encuentra (404) y los fallos de conexión.

La pronunciación hablada no se obtiene como un archivo de audio de la API: se genera localmente mediante el servicio **TextToSpeech de Android**, configurado para utilizar inglés británico (Locale.UK).

Además, es posible iniciar nuevas búsquedas seleccionando los chips de sinónimos y antónimos. En ese caso, se conserva temporalmente la palabra anterior para permitir volver a ella mediante la navegación hacia atrás.

## 4. Persistencia de datos en Firebase
La aplicación utiliza dos servicios de Firebase con finalidades diferentes: **Realtime Database** para el historial de búsquedas y **Cloud Firestore** para el vocabulario personal de cada usuario.

### 4.1. Historial de búsquedas (Realtime Database)
Se utiliza Firebase Realtime Database para registrar las búsquedas realizadas correctamente. Cada registro contiene la palabra consultada y la fecha de la búsqueda, obtenida mediante la marca de tiempo del servidor (`ServerValue.TIMESTAMP`).

Los registros se organizan por usuario, utilizando su UID como identificador. Se genera una nueva entrada por cada búsqueda, incluso cuando se consulta varias veces la misma palabra. Las búsquedas que no obtienen un resultado válido de la API no se registran.

Desde la pantalla del historial es posible recuperar una palabra para volver a consultarla o eliminar un registro mediante un gesto lateral.

### 4.2. Vocabulario personal (Cloud Firestore)
Se utiliza Cloud Firestore para almacenar las palabras que cada usuario desea conservar. Los datos se organizan en la colección users, con un documento por usuario identificado mediante su UID y una subcolección savedWords.

Cada palabra guardada almacena su información léxica (palabra, entradas y fuente), junto con la fecha de guardado (`FieldValue.serverTimestamp()`).

El botón de guardado permite alternar entre guardar y quitar una palabra. Antes de añadirla, se comprueba si ya existe en el vocabulario del usuario, evitando así guardar varias veces la misma palabra. Los usuarios anónimos deben convertir su cuenta en una cuenta permanente antes de utilizar esta funcionalidad.

La pantalla del vocabulario permite consultar las palabras guardadas, eliminarlas individualmente y seleccionar una para realizar una nueva búsqueda.

### 4.3. Estadísticas del vocabulario

La aplicación muestra estadísticas calculadas a partir de las palabras guardadas: el número total de palabras y su distribución por categorías gramaticales. Estas estadísticas se calculan a partir de los datos recuperados de Firestore, sin almacenarse como registros independientes.

### 4.4. Justificación del uso de dos bases de datos
Aunque ambas bases de datos permiten almacenar información, se han elegido para gestionar dos tipos de datos con características y necesidades diferentes.

**Realtime Database** utiliza una estructura de datos jerárquica basada en JSON, por lo que resulta adecuada para almacenar el historial como un conjunto de registros sencillos asociados a cada usuario. Cada búsqueda genera una nueva entrada con la palabra consultada y su fecha, sin necesidad de realizar consultas complejas sobre su contenido.

Por otro lado, **Cloud Firestore** organiza la información en colecciones y documentos, ofreciendo mayor flexibilidad para almacenar los datos más completos de cada palabra y realizar consultas sobre ellos. Esto facilita operaciones como comprobar si una palabra ya está guardada, recuperar el vocabulario ordenado por fecha o eliminar documentos individualmente.