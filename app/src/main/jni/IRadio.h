#pragma once

class IRadio {
  public:
  enum State { START, STOP };

  enum Band {
    FM_EU,
    FM_US,
    FM_UU,
    AM
  };

  enum StereoState {
    STEREO,
    MONO
  };

  virtual ~IRadio() {}

  virtual bool setState(State state) = 0;
  virtual State getState() = 0;

  virtual int getFrequency() = 0;
  virtual void setFrequency(const int freq) = 0;
  virtual Band getBand() = 0;
  virtual void setBand(Band band) = 0;
  virtual int getSeekState() = 0;
  virtual void setSeekState(int state) = 0;
  virtual State getAFState() = 0;
  virtual void setAFState(State state) = 0;
  virtual State getRDSState() = 0;
  virtual void setRDSState(State state) = 0;

  virtual int getVolume() = 0;
  virtual void setVolume(const int volume) = 0;

  static IRadio* getRadio();
};
