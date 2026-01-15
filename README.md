# DVSmart Files API

## 📋 Descripción

**dvsmart_files_api** es un microservicio REST para búsqueda, visualización y descarga de archivos PDF desde el servidor SFTP destino. Los archivos están organizados mediante hash partitioning por `dvsmart_reorganization_api` para acceso O(1).

## 🏗 Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                     dvsmart_files_api                            │
├─────────────────────────────────────────────────────────────────┤
│  📥 Adaptadores IN          │  📤 Adaptadores OUT               │
│  ├─ FileController          │  ├─ FileMetadataMongoAdapter      │
│  └─ MonitoringController    │  └─ SftpFileContentAdapter        │
├─────────────────────────────────────────────────────────────────┤
│  🎯 DOMINIO                                                      │
│  ├─ Modelos: FileMetadata, SearchCriteria, PagedResult          │
│  ├─ Servicios: FileSearchService, FileDownloadService           │
│  └─ Puertos: FileMetadataPort, FileContentPort                  │
└─────────────────────────────────────────────────────────────────┘
           │                                    │
           ▼                                    ▼
    ┌─────────────┐                    ┌─────────────────┐
    │  MongoDB    │                    │  SFTP Destino   │
    │ files_index │                    │ /organized_data │
    └─────────────┘                    └─────────────────┘
```

## 🛠 Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje base |
| Spring Boot | 3.4.1 | Framework |
| Spring Data MongoDB | - | Acceso a datos |
| Spring Integration SFTP | 6.4.1 | Conexión SFTP |
| Apache PDFBox | 3.0.3 | Preview de PDFs |
| Apache Commons Pool2 | 2.12.0 | Pool de conexiones |

## 🚀 Instalación

### Requisitos

- JDK 21
- Maven 3.8+
- MongoDB 5.0+
- Servidor SFTP destino

### Compilación

```bash
# Clonar repositorio
git clone <repository-url>
cd dvsmart_files_api

# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/dvsmart_files_api.jar
```

### Configuración

Editar `src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://user:pass@host:27017/dvsmart-ms

# SFTP Destino
sftp.dest.host=sftp-destination-host
sftp.dest.port=22
sftp.dest.user=sftpdestinationuser
sftp.dest.password=securepass
sftp.dest.base-dir=/organized_data

# Pool
sftp.dest.pool.max-size=20
sftp.dest.pool.test-on-borrow=true
```

## 📡 API Endpoints

### Búsqueda de Archivos

```http
GET /api/files/search?q=factura&tipoDocumento=FACTURA&page=0&size=20
```

**Parámetros:**
- `q`: Búsqueda por nombre (parcial, case-insensitive)
- `tipoDocumento`: Filtro por tipo (FACTURA, CONTRATO, etc.)
- `codigoCliente`: Filtro por cliente
- `anio`: Filtro por año
- `mes`: Filtro por mes (1-12)
- `page`: Página (default: 0)
- `size`: Tamaño (default: 20, max: 100)
- `sort`: Campo (fileName, fileSize, lastModificationDate)
- `direction`: asc/desc

**Response:**
```json
{
  "content": [
    {
      "idUnico": "a1b2c3d4...",
      "fileName": "factura_001.pdf",
      "fileSize": 1048576,
      "fileSizeFormatted": "1.00 MB",
      "tipoDocumento": "FACTURA",
      "codigoCliente": "CLI001"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1500,
  "totalPages": 75,
  "hasNext": true
}
```

### Obtener Metadata

```http
GET /api/files/{idUnico}
```

### Descargar PDF

```http
GET /api/files/{idUnico}/download
```

Response: `Content-Disposition: attachment`

### Ver PDF en Navegador

```http
GET /api/files/{idUnico}/view
```

Response: `Content-Disposition: inline`

### Preview (Imagen)

```http
GET /api/files/{idUnico}/preview?width=300&height=400&page=1&format=png
```

### Estadísticas

```http
GET /api/files/stats
```

### Monitoreo Pool SFTP

```http
GET /api/monitoring/sftp-pool
GET /api/monitoring/sftp-pool/health
```

## 📊 Monitoreo

### Actuator Endpoints

```bash
# Health
curl http://localhost:8080/dvsmart_files_api/actuator/health

# Metrics
curl http://localhost:8080/dvsmart_files_api/actuator/metrics
```

### Swagger UI

```
http://localhost:8080/dvsmart_files_api/swagger-ui.html
```

## 🔧 Configuración de Alto Rendimiento

```properties
# JVM
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"

# Pool SFTP
sftp.dest.pool.max-size=30
sftp.dest.pool.test-on-borrow=true

# Download buffer
files.download.buffer-size=16384
```

## 📁 Estructura del Proyecto

```
src/main/java/com/indra/minsait/dvsmart/files/
├── DvsmartFilesApiApplication.java
├── adapter/
│   └── in/rest/
│       ├── FileController.java
│       ├── MonitoringController.java
│       ├── GlobalExceptionHandler.java
│       ├── dto/
│       │   ├── FileSearchRequest.java
│       │   ├── FileResponse.java
│       │   ├── PagedSearchResponse.java
│       │   └── ...
│       └── mapper/
│           └── SearchCriteriaMapper.java
├── domain/
│   ├── exception/
│   │   ├── FileNotFoundException.java
│   │   ├── FileNotAvailableException.java
│   │   └── ...
│   ├── model/
│   │   ├── FileMetadata.java
│   │   ├── SearchCriteria.java
│   │   └── ...
│   ├── port/
│   │   ├── in/
│   │   │   ├── SearchFilesUseCase.java
│   │   │   ├── DownloadFileUseCase.java
│   │   │   └── ...
│   │   └── out/
│   │       ├── FileMetadataPort.java
│   │       └── FileContentPort.java
│   └── service/
│       ├── FileSearchService.java
│       ├── FileDownloadService.java
│       └── PdfPreviewService.java
└── infrastructure/
    ├── config/
    │   ├── SftpConfigProperties.java
    │   ├── FilesConfigProperties.java
    │   └── ...
    ├── persistence/
    │   ├── adapter/
    │   │   └── FileMetadataMongoAdapter.java
    │   ├── document/
    │   │   └── FileIndexDocument.java
    │   ├── mapper/
    │   │   └── FileMetadataMapper.java
    │   └── repository/
    │       └── FileIndexMongoRepository.java
    └── sftp/
        ├── CustomLazySftpSessionFactory.java
        └── adapter/
            └── SftpFileContentAdapter.java
```

## 🤝 Ecosistema DVSmart

| Microservicio | Función |
|---------------|---------|
| `dvsmart_indexing_api` | Indexa archivos del SFTP origen a MongoDB |
| `dvsmart_reorganization_api` | Reorganiza archivos con hash partitioning |
| **`dvsmart_files_api`** | **Búsqueda y descarga de archivos** |

## 📞 Soporte

**Equipo**: DVSmart Team  
**Email**: dvsmart@minsait.com
