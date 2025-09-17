# Hotel Paraíso - Sistema de Gestión Hotelera

## Descripción

Sistema completo de gestión hotelera desarrollado en Java con interfaz gráfica Swing y base de datos SQLite. Permite administrar clientes, habitaciones y reservaciones con validación de disponibilidad y cálculo automático de costos.

## Características Principales

### 🏨 Gestión de Habitaciones
- **CRUD completo**: Crear, leer, actualizar y eliminar habitaciones
- **Tipos de habitación**: Simple, Doble, Suite, Familiar
- **Estados**: Disponible, Ocupada, En Mantenimiento, En Limpieza
- **Filtrado**: Por tipo y estado
- **Validación**: Números únicos, precios positivos

### 👥 Gestión de Clientes
- **CRUD completo**: Registro, búsqueda, actualización y eliminación
- **Datos**: Nombre, apellido, DNI, teléfono, email
- **Búsqueda**: Por nombre, apellido o DNI
- **Validación**: DNI único, formato de email válido

### 📅 Gestión de Reservaciones
- **CRUD completo**: Registrar, actualizar, cancelar y eliminar reservaciones
- **Validaciones**:
  - ✅ Fechas válidas (no pasadas)
  - ✅ Disponibilidad de habitación
  - ✅ No superposición de fechas
- **Cálculo automático**: Costo total basado en días y precio por noche
- **Estados**: Pendiente, Confirmada, Cancelada, Completada, No Show
- **Filtrado**: Por estado y fechas

### 💾 Base de Datos SQLite
- **Esquema completo**: Con claves foráneas y restricciones
- **Integridad referencial**: Previene eliminación de registros con dependencias
- **Datos de muestra**: Se insertan automáticamente al inicio

## Estructura del Proyecto

```
src/
├── main/java/com/hotel/
│   ├── dao/                    # Data Access Objects
│   │   ├── ClienteDAO.java
│   │   ├── HabitacionDAO.java
│   │   └── ReservacionDAO.java
│   ├── gui/                    # Interfaz Gráfica Swing
│   │   ├── MainWindow.java
│   │   ├── ClientePanel.java
│   │   ├── HabitacionPanel.java
│   │   └── ReservacionPanel.java
│   ├── model/                  # Entidades del dominio
│   │   ├── Cliente.java
│   │   ├── Habitacion.java
│   │   └── Reservacion.java
│   ├── util/                   # Utilidades
│   │   └── DatabaseConnection.java
│   └── main/                   # Aplicación principal
│       └── MainApp.java
└── test/java/com/hotel/
    └── SystemTest.java         # Pruebas del sistema
```

## Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+

### Pasos para ejecutar

1. **Clonar el repositorio**:
```bash
git clone <repository-url>
cd Hotel-para-so-gesti-n
```

2. **Compilar el proyecto**:
```bash
mvn clean compile
```

3. **Ejecutar la aplicación**:
```bash
mvn exec:java -Dexec.mainClass="com.hotel.main.MainApp"
```

4. **Ejecutar las pruebas**:
```bash
mvn test-compile
java -cp "target/classes:target/test-classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" com.hotel.test.SystemTest
```

## Funcionalidades Implementadas

### ✅ Modelo de Datos
- [x] Clase Cliente con validaciones
- [x] Clase Habitacion con tipos y estados
- [x] Clase Reservacion con cálculo de costos
- [x] Enums para tipos y estados

### ✅ Acceso a Datos (DAO)
- [x] ClienteDAO: CRUD completo con búsquedas
- [x] HabitacionDAO: CRUD con filtros y disponibilidad
- [x] ReservacionDAO: CRUD con validaciones complejas
- [x] Conexión SQLite con patrón Singleton

### ✅ Interfaz Gráfica
- [x] Ventana principal con pestañas
- [x] Panel de clientes: formulario y tabla
- [x] Panel de habitaciones: formulario, tabla y filtros
- [x] Panel de reservaciones: formulario con validaciones
- [x] Menús y barra de estado

### ✅ Validaciones
- [x] Fechas no pasadas para reservaciones
- [x] Verificación de disponibilidad de habitaciones
- [x] Prevención de reservas superpuestas
- [x] Validación de formato de email
- [x] DNI único para clientes
- [x] Números únicos para habitaciones

### ✅ Características Adicionales
- [x] Cálculo automático de costos
- [x] Búsqueda y filtrado en todas las entidades
- [x] Interfaz intuitiva con formularios claros
- [x] Manejo de errores con mensajes informativos
- [x] Datos de muestra automáticos

## Esquema de Base de Datos

### Tabla `clientes`
```sql
CREATE TABLE clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    dni TEXT UNIQUE NOT NULL,
    telefono TEXT,
    email TEXT
);
```

### Tabla `habitaciones`
```sql
CREATE TABLE habitaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero TEXT UNIQUE NOT NULL,
    tipo TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'DISPONIBLE',
    precio_por_noche DECIMAL(10,2) NOT NULL,
    capacidad INTEGER NOT NULL,
    descripcion TEXT
);
```

### Tabla `reservaciones`
```sql
CREATE TABLE reservaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id INTEGER NOT NULL,
    habitacion_id INTEGER NOT NULL,
    fecha_check_in DATE NOT NULL,
    fecha_check_out DATE NOT NULL,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE',
    costo_total DECIMAL(10,2),
    observaciones TEXT,
    fecha_reservacion DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE,
    FOREIGN KEY (habitacion_id) REFERENCES habitaciones (id) ON DELETE CASCADE
);
```

## Casos de Uso Principales

### 1. Registrar Cliente
- Ingresar datos personales
- Validar DNI único
- Confirmar registro

### 2. Crear Habitación
- Definir tipo y características
- Establecer precio
- Configurar estado inicial

### 3. Realizar Reservación
- Seleccionar cliente y habitación
- Elegir fechas de estadía
- Validar disponibilidad
- Calcular costo total automáticamente
- Confirmar reservación

### 4. Verificar Disponibilidad
- Consultar habitaciones libres por fechas
- Filtrar por tipo de habitación
- Mostrar precios y características

## Tecnologías Utilizadas

- **Java 17**: Lenguaje de programación principal
- **Swing**: Framework para interfaz gráfica
- **SQLite**: Base de datos embebida
- **Maven**: Gestión de dependencias y construcción
- **JDBC**: Conectividad con base de datos

## Patrones de Diseño

- **DAO (Data Access Object)**: Separación de lógica de datos
- **Singleton**: Para conexión a base de datos
- **MVC**: Separación de modelo, vista y controlador
- **Builder**: Para construcción de objetos complejos

## Autor

Sistema desarrollado como proyecto de gestión hotelera integral con todas las funcionalidades requeridas implementadas y probadas.