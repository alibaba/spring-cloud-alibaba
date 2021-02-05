Spring Cloud Alibaba
CircleCI Maven Central Codecov Licencia

Un proyecto mantenido por Alibaba.

Consulte el archivo Léame文档para chino.

Spring Cloud Alibaba proporciona una solución integral para el desarrollo de aplicaciones distribuidas. Contiene todos los componentes necesarios para desarrollar aplicaciones distribuidas, lo que facilita el desarrollo de sus aplicaciones con Spring Cloud.

Con Spring Cloud Alibaba, solo necesita agregar algunas anotaciones y una pequeña cantidad de configuraciones para conectar las aplicaciones Spring Cloud a las soluciones distribuidas de Alibaba, y construir un sistema de aplicaciones distribuidas con middleware Alibaba.

Características
Control de flujo y degradación del servicio : El control de flujo para servicios HTTP es compatible de forma predeterminada. También puede personalizar el control de flujo y las reglas de degradación del servicio mediante anotaciones. Las reglas se pueden cambiar de forma dinámica.
Registro y descubrimiento de servicios: el servicio se puede registrar y los clientes pueden descubrir las instancias utilizando beans administrados por Spring, cinta de integración automática.
Configuración distribuida : soporte para configuración externa en un sistema distribuido, actualización automática cuando cambia la configuración.
Impulsado por eventos : soporte para la creación de microservicios controlados por eventos altamente escalables conectados con sistemas de mensajería compartidos.
Transacción distribuida : soporte para la solución de transacciones distribuidas con alto rendimiento y facilidad de uso.
Almacenamiento de objetos en la nube de Alibaba : servicios de almacenamiento en la nube masivos, seguros, de bajo costo y altamente confiables. Soporte para almacenar y acceder a cualquier tipo de datos en cualquier aplicación, en cualquier momento y en cualquier lugar.
Alibaba Cloud SchedulerX : servicios de programación de trabajos programados precisos, altamente confiables y altamente disponibles con tiempo de respuesta en segundos.
Alibaba Cloud SMS : un servicio de mensajería que cubre todo el mundo, Alibaba SMS proporciona capacidades de comunicación convenientes, eficientes e inteligentes que ayudan a las empresas a comunicarse rápidamente con sus clientes.
Para obtener más funciones, consulte Hoja de ruta .

Componentes
Sentinel : Sentinel toma el "flujo de tráfico" como el punto de avance y proporciona soluciones en áreas como control de flujo, concurrencia, interrupción de circuitos y protección de carga para proteger la estabilidad del servicio.

Nacos : una plataforma de gestión de servicios, configuración y descubrimiento de servicios dinámicos y fácil de usar para crear aplicaciones nativas en la nube.

RocketMQ : una plataforma de transmisión y mensajería distribuida con baja latencia, alto rendimiento y confiabilidad, capacidad de un billón de niveles y escalabilidad flexible.

Dubbo : un marco RPC de código abierto basado en Java de alto rendimiento.

Seata : una solución de transacciones distribuidas con alto rendimiento y facilidad de uso para la arquitectura de microservicios.

Alibaba Cloud ACM : un centro de configuración de aplicaciones que le permite centralizar la administración de las configuraciones de las aplicaciones y lograr un empuje de configuración en tiempo real en un entorno distribuido.

Alibaba Cloud OSS : un servicio de almacenamiento en la nube cifrado y seguro que almacena, procesa y accede a cantidades masivas de datos desde cualquier parte del mundo.

Alibaba Cloud SMS : un servicio de mensajería que cubre todo el mundo, Alibaba SMS proporciona capacidades de comunicación convenientes, eficientes e inteligentes que ayudan a las empresas a contactar rápidamente a sus clientes.

Alibaba Cloud SchedulerX : servicios de programación de trabajos programados precisos, altamente confiables y altamente disponibles con tiempo de respuesta en segundos.

Para obtener más funciones, consulte Hoja de ruta .

Cómo construir
rama principal : Corresponde a Spring Cloud Greenwich y Spring Boot 2.x. Se admiten JDK 1.8 o versiones posteriores.
rama finchley : corresponde a Spring Cloud Finchley y Spring Boot 2.x. Se admiten JDK 1.8 o versiones posteriores.
Rama 1.x : Corresponde a Spring Cloud Edgware y Spring Boot 1.x, se admiten JDK 1.7 o versiones posteriores.
Spring Cloud usa Maven para la mayoría de las actividades relacionadas con la construcción, y debería poder despegar con bastante rapidez clonando el proyecto que le interesa y escribiendo:

./mvnw install
Cómo utilizar
Agregar dependencia de maven
Estos artefactos están disponibles en el repositorio de Maven Central y Spring Release a través de BOM:

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2.2.5.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
agregue el módulo en dependencies.

Documento de referencia
Contenido

Configuración de Nacos

Descubrimiento Nacos

Ejemplos
Se spring-cloud-alibaba-examplesincluye un módulo en nuestro proyecto para que pueda comenzar con Spring Cloud Alibaba rápidamente. Contiene un ejemplo, y puede consultar el archivo Léame en el proyecto de ejemplo para un recorrido rápido.

Ejemplos:

Ejemplo de centinela

Ejemplo de configuración de Nacos

Ejemplo de descubrimiento de Nacos

Ejemplo de RocketMQ

Ejemplo de Alibaba Cloud OSS

Ejemplo de Dubbo Spring Cloud

Directrices de control de versiones
El número de versión del proyecto tiene el formato xxx, donde x es un número que comienza en 0 y no se limita al rango 0 ~ 9. Cuando el proyecto está en la fase de incubadora, el número de versión es 0.xx

Como las interfaces y anotaciones de Spring Boot 1 y Spring Boot 2 se han cambiado significativamente en el módulo Actuator, y spring-cloud-commons también ha cambiado bastante de 1.xx a 2.0.0, tomamos la misma regla de versión que Número de versión de SpringBoot.

1.5.x para Spring Boot 1.5.x
2.0.x para Spring Boot 2.0.x
2.1.x para Spring Boot 2.1.x
2.2.x para Spring Boot 2.2.x
Código de conducta
Este proyecto es un subproyecto de Spring Cloud, se adhiere al código de conducta del Pacto de Colaboradores . Al participar, se espera que respete este código. Informe el comportamiento inaceptable a spring-code-of-conduct@pivotal.io .

Convenciones del código y limpieza
Ninguno de estos es esencial para una solicitud de extracción, pero todos ayudarán. También se pueden agregar después de la solicitud de extracción original pero antes de una fusión.

Utilice las convenciones de formato de código de Spring Framework. Si usa Eclipse, puede importar la configuración del formateador usando el archivo eclipse-code-formatter.xml del proyecto Spring Cloud Build. Si usa IntelliJ, puede usar el complemento Formateador de código de Eclipse para importar el mismo archivo.

Asegúrese de que todos los archivos .java nuevos tengan un comentario de clase Javadoc simple con al menos una etiqueta @author que lo identifique, y preferiblemente al menos un párrafo sobre el propósito de la clase.

Agregue el comentario del encabezado de la licencia ASF a todos los archivos .java nuevos (copie de los archivos existentes en el proyecto)

Agréguese como @autor a los archivos .java que modifique sustancialmente (más que cambios estéticos).

Agregue algunos Javadocs y, si cambia el espacio de nombres, algunos elementos de documento XSD.

Algunas pruebas unitarias también ayudarían mucho: alguien tiene que hacerlo.

Si nadie más está usando su rama, vuelva a basarla con el maestro actual (u otra rama de destino en el proyecto principal).

Cuando escriba un mensaje de confirmación, siga estas convenciones; si está solucionando un problema existente, agregue Fixes gh-XXXX al final del mensaje de confirmación (donde XXXX es el número de problema).

Contáctenos
Se recomienda la lista de correo para discutir casi cualquier tema relacionado con spring-cloud-alibaba.

spring-cloud-alibaba@googlegroups.com : puede hacer preguntas aquí si encuentra algún problema al usar o desarrollar spring-cloud-alibaba.
