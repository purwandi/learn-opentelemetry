package messages

import (
	"encoding/json"

	"github.com/shopspring/decimal"
)

type Product struct {
	ID        int             `json:"id"`
	Name      string          `json:"name"`
	Price     decimal.Decimal `json:"price"`
	Telemetry *Telemetry      `json:"telemetry"`
}

func (p *Product) ToBytes() []byte {
	byt, err := json.Marshal(p)
	if err != nil {
		return nil
	}

	return byt
}
