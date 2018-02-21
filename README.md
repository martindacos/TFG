# Sistema de análisis de conformidad en modelos de procesos

Utilización de "aligments" para calcular las métricas de conformidad de distintos modelos de procesos. Se utiliza los algoritmos de búsqueda A* y AD* para explorar los distintos nodos. Se utiliza una heurística optimista, que nos asegura que vamos a encontrar la solución óptima.

## Getting Started

Estas instrucciones te permitirán ejecutar el proyecto en tu máquina local.

### Prerequisites

* Java
* Maven

### Installing

Utilizar Maven para generar un .jar ejecutable

```
mvn package
```

Una vez que tenemos generado el ejecutable, podremos lanzarlo con dos parámetros como argumentos: un .xes con las trazas del log y un .hn con el modelo.

```
java -cp target/TFG1-1.0-SNAPSHOT.jar es.usc.citius.aligments.mains.MainA /home/martin/Descargas/PLG_Logs/10_Actividades/1000.xes /home/martin/Descargas/PLG_Logs/10_Actividades/BestIndividual.hn
```
Si lo que queremos es utilizar este código en otro proyecto, utilizamos el "artifacts". Vamos /clasess/artifacts/aligments_jar y encontraremos el .jar para importar en el proyecto (TFG1.jar). Para importarlo podemos utilizar Maven, instalándolo en nuestro repositorio local y añadiendo dicha dependencia al archivo "pom.xml".

*Nota: en la ejecución se recomienda incluír los comandos pertinentes para dotar al programa de una mayor memoria (por ejemplo, aumentamos el tamaño máximo del Heap con -Xmx4096m).

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Martín Dacosta Salgado**
