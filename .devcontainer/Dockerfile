FROM maven:3.9-eclipse-temurin-17

# Install tools we need
RUN apt update
RUN apt install -y python3-pip
RUN pip3 install pandas matplotlib

# Clone and build Choral
RUN git clone https://github.com/choral-lang/choral /choral
WORKDIR /choral
RUN mvn install
ENV CHORAL_HOME /choral/dist/target
ENV PATH "/choral/scripts:${PATH}"
