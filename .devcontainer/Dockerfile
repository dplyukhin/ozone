FROM maven:3.8-eclipse-temurin-19

# Install tools we need
RUN apt update
RUN apt install -y time parallel ncat python3-pip gnuplot

# Clone and build Choral
RUN git clone https://github.com/choral-lang/choral /choral
WORKDIR /choral
RUN mvn install
ENV CHORAL_HOME /choral/dist/target
ENV PATH "/choral/scripts:${PATH}"

# Clone irctest
RUN git clone https://github.com/progval/irctest /irctest
WORKDIR /irctest
RUN pip3 install --user -r requirements.txt
ENV PATH "/root/.local/bin:${PATH}"
