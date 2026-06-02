FROM node:22-alpine
WORKDIR /app

COPY package.json package-lock.json* bun.lock* ./
RUN npm install --frozen-lockfile 2>/dev/null || npm install

COPY . .

ENV VITE_API_BASE_URL=""

EXPOSE 5173

CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
