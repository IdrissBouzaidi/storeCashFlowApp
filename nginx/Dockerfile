FROM nginx:1.26-alpine

# Supprime la conf par défaut
RUN rm /etc/nginx/conf.d/default.conf

# Copie notre conf
COPY conf.d/* /etc/nginx/conf.d

# expose 80
EXPOSE 80

# Pas d'entrypoint custom — on utilise l'entrypoint officiel