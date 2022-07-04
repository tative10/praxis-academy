import numpy as np
import statistics as sts
import scipy.signal as scp
def filter(data_raw,fsamp):
    h = scp.firwin(120,[0.1, 10],window="blackman",pass_zero="bandpass",fs=fsamp)
    hasil_filter = scp.filtfilt(h,1,data_raw)
    return hasil_filter

def usefft(signal,fsamp):
    y = np.fft.fft(signal)
    yabs = abs(y)
    f = np.linspace(0,fsamp,len(yabs))
    return yabs[0:int(round(len(yabs)/2))],f[0:int(round(len(yabs)/2))]

fsamp = 256
data_read = np.genfromtxt("D:/temp.csv", delimiter = "\t")
hasil_fft,ff = usefft(data_read[:],fsamp)

data_filter = filter(data_read[:],fsamp)
hasil_ff_filter,ff_filter = usefft(data_filter,fsamp)

#find peak, bpm, and hrv analysis
x = data_filter [:]
#find peaks
peaks,_ = scp.find_peaks(x, distance=200)

#find RR Interval, Mean RR, and BPM
ndf = np.diff(peaks)
RRint = ndf/512
MeanRR = sts.mean(RRint)
BPM = 60/MeanRR
SDRR = sts.pstdev(ndf)
CVRR = (SDRR/MeanRR)*100