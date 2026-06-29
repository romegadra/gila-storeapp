import { CheckCircle2, X } from 'lucide-react';
import { money } from '../utils/formatters.js';

export function PurchaseToast({ receipt, onClose }) {
  if (!receipt) {
    return null;
  }

  return (
    <aside className="purchase-toast" role="status">
      <CheckCircle2 size={22} />
      <div>
        <strong>Purchase confirmed</strong>
        <span>Order #{receipt.id} - ${money(receipt.total)} paid</span>
      </div>
      <button className="icon-button" type="button" onClick={onClose} title="Dismiss confirmation">
        <X size={16} />
      </button>
    </aside>
  );
}
