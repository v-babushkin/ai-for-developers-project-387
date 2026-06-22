FROM node:24-alpine AS frontend-builder
WORKDIR /app
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
ENV VITE_API_BASE_URL=""
RUN npm run build

FROM gradle:8.5-jdk17 AS backend-builder
WORKDIR /app
COPY backend/gradle.properties backend/settings.gradle backend/build.gradle ./
COPY backend/src ./src
COPY --from=frontend-builder /app/dist ./src/main/resources/static
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE ${PORT:-8080}
COPY --from=backend-builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
