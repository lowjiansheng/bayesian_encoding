# Bayesian Network Encoder

## User Guide
### Prerequisites 
Our project was built with Gradle. Hence, please first have Gradle installed.

Once Gradle is installed, run the following commands in the project root directory to build the project jar.

## Running the encoder

1. Navigate to project’s root folder.
2. Run gradle build to build the project. 
3. Run the compiled encoder. java -cp build/classes/java/main/ Main input/{model_file_name} input/{evidence_file_name}  {encoder_type}
4. For encoder_type, choose between CLASS_ENCODER or ALT_ENCODER. 