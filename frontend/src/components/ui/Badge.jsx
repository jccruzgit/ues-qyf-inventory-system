const variantClasses = {
  neutral: 'bg-surface-2 text-copy',
  success: 'bg-[#e5f7ef] text-[#249b66]',
  danger: 'bg-[#fdebec] text-[#d53a43]',
  warning: 'bg-[#fff3dd] text-[#d28a19]',
  navy: 'bg-brand-ink text-white',
  teal: 'bg-brand-teal-soft text-brand-teal',
};

function Badge({ children, variant = 'neutral', className = '' }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-extrabold tracking-tight ${variantClasses[variant]} ${className}`}
    >
      {children}
    </span>
  );
}

export default Badge;
