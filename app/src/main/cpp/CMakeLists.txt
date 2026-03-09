# Versión mínima de CMake requerida
cmake_minimum_required(VERSION 3.22.1)

# Nombre del proyecto
project("parcial")

# =====================================================
# Configuración de OpenCV
# =====================================================

# Ruta hacia OpenCV (ajusta según tu proyecto)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../../../sdk/native/jni)

# Buscar OpenCV
find_package(OpenCV REQUIRED CONFIG)

# Incluir directorios de OpenCV
include_directories(${OpenCV_INCLUDE_DIRS})

# =====================================================
# Biblioteca nativa
# =====================================================

# Agregar la librería compartida nativa (native-lib.cpp)
add_library(native-lib SHARED
    native-lib.cpp
)

# =====================================================
# Librerías a enlazar
# =====================================================

target_link_libraries(
    native-lib
    ${OpenCV_LIBS}   # Librerías de OpenCV
    android          # Librería del sistema Android
    log              # Librería de logs
)
