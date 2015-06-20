#pragma once
#include "IRadio.h"

class Radio : public IRadio {
  public:
  Radio();
  virtual ~Radio() {}

  virtual bool setState(State state);
  virtual State getState();

  virtual int getFrequency();
  virtual void setFrequency(const int freq);
  virtual Band getBand();
  virtual void setBand(Band band);
  virtual int getSeekState();
  virtual void setSeekState(int state);
  virtual State getAFState();
  virtual void setAFState(State state);
  virtual State getRDSState();
  virtual void setRDSState(State state);
  virtual int getVolume();
  virtual void setVolume(const int volume);
  virtual int readRDS();

  private:
  enum CODEC_Level {
    CODEC_LEVEL_NO_ANALOG = 0,
    CODEC_LEVEL_BT_MUSIC = 1,
    CODEC_LEVEL_AV_IN = 2,
    CODEC_LEVEL_DVB = 3,
    CODEC_LEVEL_DVD = 4,
    CODEC_LEVEL_RADIO = 5,
    CODEC_LEVEL_BT_TEL = 6,
    CODEC_LEVEL_COUNT
  };

  unsigned int checkSum(unsigned char* cmdBuf, int size) const;
  bool send_command(const char cmd, const char param1, const char param2) const;
  bool band_set(int low, int high, int band, int step_len);
  bool activeAudio(CODEC_Level codec_mode);

  bool seek_stop();
  bool seek_start(bool up);

  bool recoverAudio(CODEC_Level codec_mode);
  bool close_dev();
  bool open_dev();

  int fd_radio;
  int fd_bonovo;
  int fd_rds;
  int m_SeekState;
  State m_state;
  State m_AFState;
  State m_RDSState;
  Band m_band;
  int m_Frequency;
  int m_Volume;
  bool m_LastSeekDirection;
};
