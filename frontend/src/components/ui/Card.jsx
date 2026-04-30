function Card({ children, className = '' }) {
  return (
    <section
      className={`institutional-surface rounded-[30px] border border-white/80 p-6 shadow-[0_18px_38px_rgba(23,61,44,0.08)] ${className}`}
    >
      {children}
    </section>
  );
}

export default Card;
