# Central de Emergencias — Kafka (semana 14)

Proyecto docente para viernes 13 de noviembre. Basado en el `docker-compose.yaml` entregado por el docente: **Confluent Kafka 7.5.0 + ZooKeeper + Kafka UI 8082**. No reemplazar el Compose por un ejemplo KRaft.

## Requisitos
- Docker Desktop encendido y `docker compose` disponible.
- JDK 17 o 21 (compilación dirigida a Java 17).
- Maven 3.9+ instalado, o importar `pom.xml` en IntelliJ / VS Code con soporte Maven. **Este ZIP no incluye Maven Wrapper**.
- Puertos 9092, 8082 y 8083 disponibles.

## Paso 1 — infraestructura
Abrir terminal en `infra/kafka`:

```bash
docker compose up -d
docker compose ps
docker compose exec -T kafka kafka-topics --bootstrap-server kafka:29092 --list
```

Kafka UI: http://localhost:8082, clúster `laboratorio`.

## Paso 2 — topics (se pueden crear antes de iniciar Java)
Desde `infra/kafka`:

```bash
docker compose exec -T kafka kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic emergencias --partitions 3 --replication-factor 1
docker compose exec -T kafka kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic emergencias-auditoria --partitions 2 --replication-factor 1
docker compose exec -T kafka kafka-configs --bootstrap-server kafka:29092 --entity-type topics --entity-name emergencias --alter --add-config retention.ms=604800000,cleanup.policy=delete
docker compose exec -T kafka kafka-configs --bootstrap-server kafka:29092 --entity-type topics --entity-name emergencias-auditoria --alter --add-config retention.ms=2592000000,cleanup.policy=delete
```

También se incluye `crear-y-verificar-topics.sh` (Mac/Linux) que automatiza estos pasos. **Si el topic ya existe**, la configuración de retención se actualiza con `kafka-configs`; la creación con `--if-not-exists` no modifica topics existentes. El bean `NewTopic` en Java sirve para declarar/crear topics, pero no reemplaza la alteración explícita de configuraciones en topics preexistentes.

## Paso 3 — Spring Boot
Abrir `backend/emergencias-api` como proyecto Maven. Si Maven está instalado:

```bash
mvn spring-boot:run
```

No usar `./mvnw` ni `mvnw.cmd` porque el wrapper no está incluido. Se puede ejecutar desde el IDE. La aplicación usa `localhost:9092` porque corre en el computador, no en Docker. `KafkaTopicConfig` declara los dos topics, pero la API publica **solo en `emergencias`** por ahora.

## Paso 4 — POST con Postman
URL: `POST http://localhost:8083/api/emergencias`
Header: `Content-Type: application/json`
Body:

```json
{"comuna":"Quilicura","tipo":"INCENDIO","descripcion":"Incendio en una bodega"}
```

Respuesta esperada: HTTP 202 con ID, fecha, comuna, tipo y descripción. En consola: partición y offset. En Kafka UI, revisar `emergencias` > Messages.

## Paso 5 — consumer groups y lag (demostración para el lunes)
Para generar mensajes en auditoría desde `infra/kafka`:

```bash
printf 'Evento-1
Evento-2
Evento-3
' | docker compose exec -T kafka kafka-console-producer --bootstrap-server kafka:29092 --topic emergencias-auditoria
```

Terminal A:
```bash
docker compose exec -T kafka kafka-console-consumer --bootstrap-server kafka:29092 --topic emergencias-auditoria --group grupo-reporteria --from-beginning
```

Terminal B:
```bash
docker compose exec -T kafka kafka-console-consumer --bootstrap-server kafka:29092 --topic emergencias-auditoria --group grupo-auditoria --from-beginning
```

Esperar al menos unos segundos para que los consumidores confirmen offsets, detener con Ctrl+C y publicar más mensajes. Consultar:

```bash
docker compose exec -T kafka kafka-consumer-groups --bootstrap-server kafka:29092 --describe --group grupo-reporteria
```

El lag depende de los offsets confirmados; no equivale necesariamente a procesamiento de negocio completado.

## Por qué incluimos jackson-databind
El martes apareció `NoClassDefFoundError: com/fasterxml/jackson/databind/JavaType`. Este proyecto fija Spring Boot 3.5.7 y utiliza la versión compatible de Spring Kafka administrada por Boot, junto a `jackson-databind` **sin versión manual**. Evitar mezclar Spring Kafka 4.x con el serializador Jackson 2 de este ejemplo. No copiar versiones de dependencias de un POM antiguo.

## Arquitectura
`Controller -> Service -> Producer -> Kafka`. `config/kafka` contiene declaración de topics, `dto` entradas, `model` evento, `messaging/producer` publicación y `messaging/consumer` queda reservado para el lunes. No hay persistencia de base de datos ni consumer implementado en este ZIP.

## Problemas frecuentes
- `No such container`: usar `docker compose exec -T kafka` desde `infra/kafka`, no `docker exec kafka`.
- `Connection refused`: comprobar `docker compose ps`, que Docker Desktop esté activo y puerto 9092.
- `NoClassDefFoundError JavaType`: importar de nuevo el POM y verificar que se ejecuta este proyecto, no el del martes con Spring Kafka 4.x.
- `Topic already exists`: normal con `--if-not-exists`.
- `9092` es broker, `8082` es Kafka UI y `8083` es la API Java.
