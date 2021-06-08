FROM ubuntu:18.04

ENV TZ=Europe/Warsaw

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt-get -qq update && apt upgrade -y \
    && apt-get -qq install --no-install-recommends -y \
    gnupg \
    curl \
    wget \
    unzip \
    vim

RUN apt-get -qq install --no-install-recommends -y openjdk-8-jdk

ENV SCALA_VERSION=2.12.10

RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" \
    | tee /etc/apt/sources.list.d/sbt.list \
    && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add \
    && apt-get update \
    && apt-get install -qq -y sbt \
    && wget http://www.scala-lang.org/files/archive/scala-$SCALA_VERSION.deb \
    && dpkg -i scala-$SCALA_VERSION.deb && rm scala-$SCALA_VERSION.deb \
    && apt-get clean

EXPOSE 9000

COPY ./play /home/ulmii/webshop/
WORKDIR /home/ulmii/webshop

RUN sbt compile
CMD sbt run
