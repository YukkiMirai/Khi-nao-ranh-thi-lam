FROM node:20

WORKDIR /app

COPY package*.json ./

RUN npm install -g pm2 \
    && npm install

COPY . .

CMD ["pm2-runtime", "bot.js"]