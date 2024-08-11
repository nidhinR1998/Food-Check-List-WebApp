# Build stage
FROM maven:3.8.2-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -Pprod -DskipTests

# Package stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/CheckListApplication-0.1.jar app.jar

# Set environment variables
ENV SPRING_DATASOURCE_URL=jdbc:mysql://foodchecklistdb.c7qe0oc64yu4.ap-south-1.rds.amazonaws.com:3306/todos
ENV SPRING_DATASOURCE_USERNAME=admin
ENV SPRING_DATASOURCE_PASSWORD=Xpulse2008035
ENV SPRING_MAIL_HOST=smtp.gmail.com
ENV SPRING_MAIL_PORT=587
ENV SPRING_MAIL_USERNAME=foodchecklistapplication@gmail.com
ENV SPRING_MAIL_PASSWORD=lfbkcyldonasjtjx
ENV TWILIO_ACCOUNT_SID=ACd0c216ad0d84d4019e5bba95d606276c
ENV TWILIO_AUTH_TOKEN=e39a7682ff942d51d9a69c31a152f6e0
ENV TWILIO_VERIFY_SERVICE_SID=VA4bcbabfb1ef08b84140b57764ba259ba
ENV ENCRYPTION_KEY=PJHwUgO+5FAedcKjzcRMH5xZeFsrI2P1sP4qzMBJDX0=
ENV APPLICATION_BASE-URL=https://food-check-list.up.railway.app
ENV APPLICATION_RESET-PASSWORD-PATH=/userDetails/resetPassword

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
