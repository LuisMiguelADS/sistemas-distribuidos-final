FROM eclipse-temurin:21-jdk as builder

WORKDIR /app
COPY . .
RUN ./gradlew clean build

FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-cp", "app.jar"]
CMD ["ServidorTemperaturaQuarto1"]

# Sequência de comandos

# Para criar uma imagem:
# docker build -t servidor-temperatura-quarto1 .
# - Nessa parte é definido o nome da imagem, que no caso foi "servidor-temperatura-quarto1"

# Para criar o container
# docker run --network rede-servidores -it --name cont-server-temp-quarto1 servidor-temperatura-quarto1
# - Quando cria o container já executa a classe ServidorTemperaturaQuarto1

# Para iniciar a classe RetornaDadosEnviados
# docker exec -it cont-server-temp-quarto1 java -cp /app/app.jar RetornarDadosEnviados
# Como tem que executar outra classe do container, utilizamos essa linha de comando para executá-la
# Foi necessário porque ambas as classes tem interação com o terminal, tanto para visualização, quanto para interação com o menu de dados

