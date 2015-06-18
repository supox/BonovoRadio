#include "IRadio.h"
#include "Radio.h"

IRadio* IRadio::getRadio() {
  static IRadio* radio = new Radio();
  return radio;
}
