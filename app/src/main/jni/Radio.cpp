#include "Radio.h"
#include "Logger.h"

#include <stdio.h>
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <sys/ioctl.h>

#include "BonovoDefs.h"

Radio::Radio() :
  fd_radio(-1),
  fd_bonovo(-1),
  fd_rds(-1),
  m_SeekState(STOP),
  m_state(STOP),
  m_AFState(STOP),
  m_RDSState(STOP),
  m_band(FM_EU),
  m_Frequency(0),
  m_Volume(100),
  m_LastSeekDirection(false) {
}

bool Radio::setState(State state) {
  switch (state) {
    case START:
      if (!open_dev()) return false;
      setVolume(m_Volume);
      break;
    case STOP:
      if (!close_dev()) return false;
      break;
  }
  m_state = state;
  return true;
}

IRadio::State Radio::getState() { return m_state; }
Radio::Band Radio::getBand() { return m_band; }

void Radio::setBand(Band band) {
  m_band = band;

  int freq_lo = 8700;
  int freq_hi = 10800;

  if (band == FM_UU) freq_lo = 7600;

  int freq_inc = 5;
  if (band == FM_US) freq_inc = 10;

  if(!band_set(freq_lo, freq_hi, band, freq_inc)) {
    Logger::Error("Failed setting band.");
  }
}

int Radio::getSeekState() { return m_SeekState; }

void Radio::setSeekState (int state) {
  switch (state) {
  case 1: // up
      seek_start(true);
      break;
  case 2: // down
      seek_start(false);
      break;
    default:
      seek_stop();
      break;
  }
  m_SeekState = state;
}

Radio::State Radio::getAFState() { return m_AFState; }

void Radio::setAFState(State state) {}

Radio::State Radio::getRDSState() { return m_RDSState; }

void Radio::setRDSState(State state) {
  if(fd_rds < 0)
    return;

  switch(state) {
  case START:
    if(ioctl(fd_rds, RDS_IOCTL_START_DATA) == 0 &&
      send_command(CMD_RADIO_RDS_ON_OFF, 0x01)) {
      Logger::Debug("Starting RDS");
      m_RDSState = state;
      }
    break;
    case STOP: default:
    if(ioctl(fd_rds, RDS_IOCTL_STOP_DATA) == 0 &&
      send_command(CMD_RADIO_RDS_ON_OFF, 0x00)) {
      Logger::Debug("Stopping RDS");
      m_RDSState = state;
      }
    break;
  }
}

int Radio::readRDS() {
  if (m_RDSState != START) {
    return -2;
  }

  unsigned char c;
  if(read(fd_rds, &c, sizeof(c)) == 1)
    return c;

  return -1;
}

int Radio::getVolume() { return m_Volume; }

const static int maxVolume = 100;
void Radio::setVolume(const int volume) {
  if (!send_command(CMD_RADIO_MUTE, (volume == 0 ? 3 : 0), 0))
   return;
  if (!send_command(CMD_RADIO_VOLUME, volume > maxVolume ? maxVolume : volume, 0)) {
    return;
  }

  m_Volume = volume;
}

char buffer[256];
const static int freqFactor = 1;
void Radio::setFrequency(const int freq) {
  snprintf(buffer, sizeof(buffer), "Setting frequency %d", freq);
  Logger::Debug(buffer);

  int freq10 = freq / freqFactor;
  if(send_command(CMD_RADIO_FREQ, freq10 & 0x0FF, (freq10 >> 8) & 0x0FF))
    m_Frequency = freq;
  else {
    snprintf(buffer, sizeof(buffer), "Failed setting frequency to %d", freq);
    Logger::Error(buffer);
  }
}

int Radio::getFrequency() {
  radio_freq temp;

  int res=1, freq=m_Frequency;
  for (int count = 0; res >= 0 && count < 20; count++) {
    res = ioctl(fd_bonovo, IOCTL_HANDLE_GET_RADIO_FREQ, &temp);
    if (res >= 0) {
      freq = freqFactor * (temp.freq[0] + (temp.freq[1] << 8));
    }
  }

  m_SeekState = STOP;
  m_Frequency = freq;

  return (m_Frequency);
}

unsigned int
Radio::checkSum(unsigned char* cmdBuf, int size) const {
  unsigned int sum = 0;
  for (int i = 0; i < size; i++) {
    sum += cmdBuf[i];
  }
  return sum;
}

bool Radio::send_command(const char cmd, const char param1,
                         const char param2) const {
  if ((fd_radio < 0) || (fd_bonovo < 0)) {
    Logger::Error("Could not send command since handle is not opened.");
    return false;
  }

  unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A,   0x00,
                              0xA1, cmd,  param1, param2};

  unsigned int sum = checkSum(cmdBuf, sizeof(cmdBuf) - 2);
  cmdBuf[8] = sum & 0xFF;
  cmdBuf[9] = (sum >> 8) & 0xFF;

  if (write(fd_radio, cmdBuf, cmdBuf[2]) < 0) {
    Logger::Error("Could not send command since write error.");
    return false;
  }
  return true;
}

bool Radio::band_set(int low, int high, int band, int step_len) {
  snprintf(buffer, sizeof(buffer), "Setting band low, high, band, step_len = %d, %d, %d, %d ", low, high, band, step_len);
  Logger::Debug(buffer);

  if (!send_command(CMD_RADIO_BAND_SELECT, band, 0))
    return false;

  if(!send_command(CMD_RADIO_MIN_FREQ, low & 0x0FF, (low >> 8) & 0x0FF))
      return false;

  if(!send_command(CMD_RADIO_MAX_FREQ, high & 0x0FF, (high >> 8) & 0x0FF))
      return false;

  if(!send_command(CMD_RADIO_STEP_LEN, step_len & 0x0FF,
                     (step_len >> 8) & 0x0FF))
     return false;

  return true;
}

bool Radio::activeAudio(CODEC_Level codec_mode) {
  if (fd_bonovo < 0) {
    Logger::Error("Could not activate audio since fd_bonovo is invalid.");
    return false;
  }
  int res = ioctl(fd_bonovo, IOCTL_HANDLE_CODEC_SWITCH_SRC, codec_mode);
  if (res != 0) {
    snprintf(buffer, sizeof(buffer), "Could not ioctl. res=%d", res);
    Logger::Error(buffer);

    return false;
  }
  return true;
}

bool Radio::seek_stop() {
  if (!send_command(CMD_RADIO_STOP_SEARCH, 0, 0))
      return false;
  m_SeekState = STOP;
  return true;
}

bool Radio::seek_start(bool up) {
  if(m_LastSeekDirection != up)
    setFrequency(m_Frequency);
  m_LastSeekDirection = up;

  const int bits = (up) ? 0x03 : 0x02;
  if (!send_command(CMD_RADIO_START_SEARCH, bits, 0))
      return false;

  ioctl(fd_bonovo, IOCTL_HANDLE_START_RADIO_SEARCH);
  m_SeekState = START;

  return getFrequency() != 0;
}

bool Radio::recoverAudio(CODEC_Level codec_mode) {
  if (fd_bonovo < 0) {
    return false;
  }
  return ioctl(fd_bonovo, IOCTL_HANDLE_CODEC_RECOVER_SRC, codec_mode) == 0;
}

bool Radio::close_dev() {
  if (fd_radio < 0) {
    return false;
  }
  recoverAudio(CODEC_LEVEL_RADIO);

  if (!send_command(CMD_RADIO_SHUTDOWN, 0, 0)) {
    return false;
  }

  close(fd_radio);
  close(fd_bonovo);

  return true;
}

bool Radio::open_dev() {
  fd_radio = open(RADIO_DEV_NODE, O_RDWR | O_NOCTTY | O_NONBLOCK);
  if (fd_radio < 0) {
    Logger::Error("Could not open radio dev node");
    return false;
  }

  fd_bonovo = open(AUDIO_CTRL_NODE, O_RDWR | O_NOCTTY | O_NONBLOCK);
  if (fd_bonovo < 0) {
    Logger::Error("Could not open bonovo dev node");
    return false;
  }

  fd_rds = open(RDS_CTRL_NODE, O_RDWR | O_NOCTTY | O_NONBLOCK);
  if (fd_rds < 0) {
    Logger::Debug("Could not open rds dev node, skipping");
  }

  if (!activeAudio(CODEC_LEVEL_RADIO)) {
    Logger::Error("Could not activate audio");
    return false;
  }

  Logger::Debug("Opened dev");
  return true;
}

