import pandas as pd
import matplotlib.pyplot as plt
import glob
import re
import sys


####################################################################################################
# Senders
####################################################################################################

def plot_cdf_from_csv(filename, inputs, show):
    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(5, 5))
    axes = axes.flatten()

    for i, title in enumerate(inputs.keys()):
        file_path = inputs[title]
        # Reading data from CSV file
        data = pd.read_csv(file_path, header=None)
        # Ignore the first 100 entries; warming up the JVM
        data = data.iloc[100:]
        # Sort
        sorted_data = data[0].sort_values()
        # Compute the CDF values
        cdf = sorted_data.rank(method='first') / len(sorted_data)

        axes[i].plot(sorted_data, cdf, marker='.', linestyle='none')
        axes[i].set_xlabel('Data')
        axes[i].set_ylabel('CDF')
        axes[i].set_xlim(0, 15)

    plt.tight_layout()
    if show:
        plt.show()
    else:
        plt.savefig(filename, format='png')
        print(f'Plot saved to {filename}.')

def plot_histogram_from_csv(filename, inputs, bins, show):
    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(5, 5))
    axes = axes.flatten()

    maxima = [0, 0, 0, 0]

    for i, title in enumerate(inputs.keys()):
        file_path = inputs[title]
        # Reading data from CSV file
        data = pd.read_csv(file_path, header=None)
        # Ignore the first 100 entries; warming up the JVM
        data = data.iloc[100:]
        # Convert each datum to integer
        #data = data.astype(int)

        maxima[i] = data[0].quantile(0.999)

        # Plotting histogram
        color = 'red' if 'Ozone' in title else 'blue'
        axes[i].hist(data[0], bins=bins, color=color, alpha=0.7)
        axes[i].axvline(data[0].mean(), color='red', linestyle='dashed', linewidth=1)
        axes[i].set_title(title)
        axes[i].set_xlabel('Latency (ms)')
        axes[i].set_ylabel('Frequency')

    # Set the same x and y limits for all subplots
    for i in range(4):
        axes[i].set_xlim(0, max(maxima))
        axes[i].set_ylim(0, 1000)

    plt.tight_layout()
    if show:
        plt.show()
    else:
        plt.savefig(filename, format='png')
        print(f'Plot saved to {filename}.')


def plot_senders_histograms(show):
    inputs = {
        'txt (Choral)': 'data/inordersend/key-latencies.csv',
        'key (Choral)': 'data/inordersend/txt-latencies.csv',
        'txt (Ozone)': 'data/concurrentsend/key-latencies.csv',
        'key (Ozone)': 'data/concurrentsend/txt-latencies.csv',
    }

    plot_histogram_from_csv("figures/Figure 19b - Concurrent senders latency.png", inputs, bins=25, show=show)


####################################################################################################
# Producers
####################################################################################################

def find_producer_rates():
    inorder_files = glob.glob('data/inorderproducers/worker1-rps*.csv')
    concurrent_files = glob.glob('data/concurrentproducers/worker1-rps*.csv')

    inorder_rates = sorted([int(re.search('rps(\d+).csv', file).group(1)) for file in inorder_files])
    concurrent_rates = sorted([int(re.search('rps(\d+).csv', file).group(1)) for file in concurrent_files])

    return inorder_rates, concurrent_rates


def plot_producer_latency(show):
    choral_rates, ozone_rates = find_producer_rates()

    choral_median = []
    choral_99pi = []
    ozone_median = []
    ozone_99pi = []

    for rate in choral_rates:
        path1 = f'data/inorderproducers/worker1-rps{rate}.csv'
        path2 = f'data/inorderproducers/worker2-rps{rate}.csv'
        data1 = pd.read_csv(path1, header=None).iloc[100:]
        data2 = pd.read_csv(path2, header=None).iloc[100:]
        data = pd.concat([data1, data2])
        choral_median.append(data[0].median())
        choral_99pi.append(data[0].quantile(0.99))

    for rate in ozone_rates:
        path1 = f'data/concurrentproducers/worker1-rps{rate}.csv'
        path2 = f'data/concurrentproducers/worker2-rps{rate}.csv'
        data1 = pd.read_csv(path1, header=None).iloc[100:]
        data2 = pd.read_csv(path2, header=None).iloc[100:]
        data = pd.concat([data1, data2])
        ozone_median.append(data[0].median())
        ozone_99pi.append(data[0].quantile(0.99))

    plt.plot(ozone_rates, ozone_median, 'o-', label='Ozone (median)', color='red')
    plt.plot(ozone_rates, ozone_99pi, 'x-', label='Ozone (99pi)', color='red')
    plt.plot(choral_rates, choral_median, 'o-', label='Choral (median)', color='blue')
    plt.plot(choral_rates, choral_99pi, 'x-', label='Choral (99pi)', color='blue')
    plt.xlabel('Requests per second')
    plt.ylabel('Latency (ms)')
    plt.legend()
    plt.tight_layout()
    if show:
        plt.show()
    else:
        filename = "figures/Figure 19a - Concurrent producers latency.png"
        plt.savefig(filename, format='png')
        print(f'Plot saved to {filename}.')


####################################################################################################
# Model serving
####################################################################################################

def find_modelserving_rates(batch_size):
    ozone_files = glob.glob('data/modelserving/throughput-concurrent-rate*-batch*.csv')
    choral_files = glob.glob('data/modelserving/throughput-inorder-rate*-batch*.csv')
    akka_files = glob.glob('data/modelserving/throughput-akka-rate*-batch*.csv')

    ozone_rates = sorted([int(re.search('rate(\d+)-batch', file).group(1)) for file in ozone_files])
    choral_rates = sorted([int(re.search('rate(\d+)-batch', file).group(1)) for file in choral_files])
    akka_rates = sorted([int(re.search('rate(\d+)-batch', file).group(1)) for file in akka_files])

    return ozone_rates, choral_rates, akka_rates


def plot_modelserving_throughput(batch_size, show):
    ozone_rates, choral_rates, akka_rates = find_modelserving_rates(batch_size)

    ozone_throughputs = []
    choral_throughputs = []
    akka_throughputs = []

    for rate in ozone_rates:
        path = f'data/modelserving/throughput-concurrent-rate{rate}-batch{batch_size}.csv'
        with open(path, 'r') as f:
            throughput = int(f.read())
            ozone_throughputs.append(throughput)

    for rate in choral_rates:
        path = f'data/modelserving/throughput-inorder-rate{rate}-batch{batch_size}.csv'
        with open(path, 'r') as f:
            throughput = int(f.read())
            choral_throughputs.append(throughput)

    for rate in akka_rates:
        path = f'data/modelserving/throughput-akka-rate{rate}-batch{batch_size}.csv'
        with open(path, 'r') as f:
            throughput = int(f.read())
            akka_throughputs.append(throughput)

    plt.figure()
    plt.plot(ozone_rates, ozone_throughputs, 'o-', label='Ozone', color='red')
    plt.plot(choral_rates, choral_throughputs, 'o-', label='Choral', color='blue')
    plt.plot(akka_rates, akka_throughputs, '--', label='Akka', color='orange')
    plt.axhline(y=308, color='green', linestyle='dotted', label='Ideal')
    plt.xlabel('Requests per second')
    plt.ylabel('Throughput\n(responses/sec)')
    plt.legend()
    plt.tight_layout()
    if show:
        plt.show()
    else:
        filename = "figures/Figure 20a - Model serving throughput.png"
        plt.savefig(filename, format='png')
        print(f'Plot saved to {filename}.')


def calculate_latency_percentile(data, percentile):
    data = data.iloc[100:]
    data = data.astype(int)
    return data[0].quantile(percentile / 100)


def plot_modelserving_99pi(batch_size, show):
    ozone_rates, choral_rates, akka_rates = find_modelserving_rates(batch_size)
    # Cut off the choral rates above 200
    choral_rates = [rate for rate in choral_rates if rate <= 200]

    ozone_median = []
    ozone_99pi = []
    choral_median = []
    choral_99pi = []
    akka_median = []
    akka_99pi = []

    for rate in ozone_rates:
        path = f'data/modelserving/latency-concurrent-rate{rate}-batch{batch_size}.csv'
        data = pd.read_csv(path, header=None)
        ozone_median.append(calculate_latency_percentile(data, 50))
        ozone_99pi.append(calculate_latency_percentile(data, 99))

    for rate in choral_rates:
        path = f'data/modelserving/latency-inorder-rate{rate}-batch{batch_size}.csv'
        data = pd.read_csv(path, header=None)
        choral_median.append(calculate_latency_percentile(data, 50))
        choral_99pi.append(calculate_latency_percentile(data, 99))

    for rate in akka_rates:
        path = f'data/modelserving/latency-akka-rate{rate}-batch{batch_size}.csv'
        data = pd.read_csv(path, header=None)
        akka_median.append(calculate_latency_percentile(data, 50))
        akka_99pi.append(calculate_latency_percentile(data, 99))

    plt.figure()
    plt.plot(ozone_rates, ozone_median, 'o-', label='Ozone (median)', color='red')
    plt.plot(ozone_rates, ozone_99pi, 'x-', label='Ozone (99pi)', color='red')
    plt.plot(choral_rates, choral_median, 'o-', label='Choral (median)', color='blue')
    plt.plot(choral_rates, choral_99pi, 'x-', label='Choral (99pi)', color='blue')
    plt.plot(akka_rates, akka_median, 'o--', label='Akka (median)', color='orange')
    plt.plot(akka_rates, akka_99pi, 'x--', label='Akka (99pi)', color='orange')
    plt.xlabel('Requests per second')
    plt.ylabel('Latency (ms)')
    plt.legend()
    plt.tight_layout()
    if show:
        plt.show()
    else:
        filename = "figures/Figure 20b - Model serving latency.png"
        plt.savefig(filename, format='png')
        print(f'Plot saved to {filename}.')


if __name__ == "__main__":
    shouldShow = '--show' in sys.argv

    plot_producer_latency(show=shouldShow)
    plot_senders_histograms(show=shouldShow)
    plot_modelserving_throughput(10, show=shouldShow)
    plot_modelserving_99pi(10, show=shouldShow)
